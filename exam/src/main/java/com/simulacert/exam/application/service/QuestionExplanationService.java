package com.simulacert.exam.application.service;

import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.dto.request.RequestExplanationCommand;
import com.simulacert.exam.application.mapper.QuestionMapper;
import com.simulacert.exam.application.port.in.QuestionExplanationUseCase;
import com.simulacert.exam.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionExplanationRun;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.service.XRayTracingService;
import com.simulacert.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionExplanationService implements QuestionExplanationUseCase {
    private static final String PROMPT_VERSION = "v1.1";
    private static final Double TEMPERATURE = 0.25;
    private static final Integer MAX_TOKENS = 1200;
    private static final Duration EXPIRATION_DURATION = Duration.ofDays(30);

    private final QuestionRepositoryPort questionRepository;
    private final QuestionExplanationRunRepositoryPort explanationRunRepository;
    private final ExplanationLLMPort llmProvider;
    private final ClockPort clock;
    private final XRayTracingService xray;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    @XRaySubsegment("llm.requestExplanation")
    public ExplanationResponse requestExplanation(RequestExplanationCommand command, UUID userId) {

        UUID questionId = command.questionId();

        xray.putAnnotation("attemptId", command.examAttemptId());
        xray.putAnnotation("questionId", questionId);

        var existing = explanationRunRepository.findAllByQuestion(questionId);

        if (existing.isPresent() && !existing.get().isEmpty()) {
            return questionMapper.toExplanationResponse(existing.get().getLast());
        }

        if (explanationRunRepository.countExplanationsByUserIdToday(userId) >= 10) {
            throw new IllegalStateException("You have reached the daily limit of 10 explanations. Please try again tomorrow.");
        }

        Question question = questionRepository.findById(questionId);

        String userPrompt = buildUserPrompt(
                question.getId(),
                question.getText(),
                question.getOptions(),
                command.certification(),
                question.getLanguage()
        );

        LLMRequest llmRequest = new LLMRequest(
                getSystemPrompt(command.certification()),
                userPrompt,
                TEMPERATURE,
                MAX_TOKENS
        );

        LLMResult llmResult = llmProvider.generate(llmRequest);

        log.info("Generated explanation using {} - {}", llmResult.provider(), llmResult.modelName());

        return createExplanationResponse(
                llmResult.content(),
                llmResult.modelName(),
                command,
                question.getLanguage(),
                UserContextHolder.getUser()
        );
    }

    @Override
    @Transactional
    @XRaySubsegment("llm.submitFeedback")
    public void submitFeedback(UUID explanationId, SubmitFeedbackCommand command) {
        xray.putAnnotation("explanationId", explanationId);
        log.info("Submitting feedback for explanation {}", explanationId);

        QuestionExplanationRun explanationRun = explanationRunRepository.findById(explanationId)
                .orElseThrow(() -> new IllegalArgumentException("Explanation not found: " + explanationId));

        explanationRun.addFeedback(command.rating(), command.comment(), clock.now(), UserContextHolder.getUser());
        explanationRunRepository.save(explanationRun);

        log.info("Feedback submitted for explanation {}: rating={}", explanationId, command.rating());
    }

    private String getSystemPrompt(String certification) {
        String year = new java.text.SimpleDateFormat("yyyy").format(new java.util.Date());
        return String.format("You are an solutions architect expert explaining exam questions for the %s certification in %s.", certification, year);
    }

    private String buildUserPrompt(UUID questionId,
                                   String text,
                                   List<QuestionOption> options,
                                   String certification,
                                   String language) {

        List<String> correctList = options.stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getOptionKey)
                .sorted(String::compareTo)
                .toList();

        if (correctList.isEmpty()) {
            throw new IllegalStateException("Question has no correct option defined: " + questionId);
        }

        String optionsText = options.stream()
                .sorted(Comparator.comparing(QuestionOption::getOptionKey))
                .map(opt -> opt.getOptionKey() + ") " + opt.getOptionText())
                .collect(Collectors.joining("\n"));

        String correctOption = String.join(", ", correctList);

        return String.format("""
                Explain why each option is correct or incorrect.
                
                Rules:
                - Use only AWS documented behavior
                - Do not restate the question or options
                - Do not add assumptions or opinions
                - Write in %s
                - Cover all options
                - Be concise and technical (2–5 sentences per option)
                
                Output (STRICT):
                - Return ONLY valid HTML
                - Start with <div class="question-explanation">
                - End with </div>
                
                Format example:
                
                <div class="question-explanation">
                  <div class="option correct|incorrect">
                    <h4>Opção A</h4>
                    <p>Explanation...</p>
                    <div class="resource">
                        <a href="https://docs.aws.amazon.com/${language:pt_br}/Route53/latest/DeveloperGuide/Welcome.html" target="_blank" rel="noopener">Route53 documentation</a>
                    </div>
                  </div>
                </div>
                
                Constraints:
                - One option block per answer (A→Z)
                - Use class="correct" or "incorrect"
                - The link is optional; include only if certain and valid
                - Do not add content outside this structure
                - If the link inside the resource div support the language specified use it, otherwise use the english version of the documentation

                Question:
                %s
                
                Options:
                %s
                
                Correct answer(s):
                %s
                
                Certification:
                %s
                        """,
                "en".equalsIgnoreCase(language) ? "english" : language,
                text,
                optionsText,
                correctOption,
                certification);
    }

    private ExplanationResponse createExplanationResponse(
            String content,
            String modelName,
            RequestExplanationCommand command,
            String language,
            UUID userId
    ) {
        Instant now = clock.now();
        Instant expiresAt = now.plus(EXPIRATION_DURATION);

        QuestionExplanationRun explanationRun = QuestionExplanationRun.create(
                command.questionId(),
                "openai", // TODO: make configurable
                modelName,
                PROMPT_VERSION,
                TEMPERATURE,
                language,
                content,
                now,
                expiresAt,
                userId
        );

        explanationRunRepository.save(explanationRun);

        return questionMapper.toExplanationResponse(explanationRun);
    }
}

