package com.simulacert.llm.application.service;

import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;
import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.RequestExplanationCommand;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.llm.application.port.in.QuestionExplanationUseCase;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.llm.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.llm.domain.QuestionExplanationRun;
import com.simulacert.service.XRayTracingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionExplanationService implements QuestionExplanationUseCase {
    private static final String PROMPT_VERSION = "v1.1";
    private static final Double TEMPERATURE = 0.25;
    private static final Integer MAX_TOKENS = 1000;
    private static final Duration EXPIRATION_DURATION = Duration.ofDays(30);

    private final AttemptRepositoryPort attemptRepository;
    private final QuestionRepositoryPort questionRepository;
    private final QuestionExplanationRunRepositoryPort explanationRunRepository;
    private final ExplanationLLMPort llmProvider;
    private final ClockPort clock;
    private final XRayTracingService xray;

    @Override
    @Transactional
    @XRaySubsegment("llm.requestExplanation")
    public ExplanationResponse requestExplanation(RequestExplanationCommand command, UUID userId) {
        xray.putAnnotation("attemptId", command.examAttemptId());
        xray.putAnnotation("questionId", command.questionId());
        log.info("Requesting explanation for question {} by user {}", command.questionId(), userId);

        validateExplanationRequest(command, userId);

        Optional<List<QuestionExplanationRun>> explanation = explanationRunRepository
                .findByQuestionIdAndLanguage(command.questionId(), command.language());

        if (explanation.isPresent() && !explanation.get().isEmpty()) {
            log.info("Returning existing explanation from DB for question {}", command.questionId());
            QuestionExplanationRun run = explanation.get().getFirst();
            return new ExplanationResponse(
                    run.getId(),
                    run.getQuestionId(),
                    run.getContent(),
                    run.getModelName(),
                    run.getExpiresAt()
            );
        }

        Question question = questionRepository.findById(command.questionId());
        String prompt = buildPrompt(question, command.certification(), command.language());

        LLMRequest llmRequest = new LLMRequest(
                getSystemPrompt(command.certification()),
                prompt,
                TEMPERATURE,
                MAX_TOKENS
        );

        LLMResult llmResult = llmProvider.generate(llmRequest);
        log.info("Generated explanation using {} - {}", llmResult.provider(), llmResult.modelName());

        return createExplanationResponse(llmResult.content(), llmResult.modelName(), command);
    }

    @Override
    @Transactional
    @XRaySubsegment("llm.submitFeedback")
    public void submitFeedback(UUID explanationId, SubmitFeedbackCommand command) {
        xray.putAnnotation("explanationId", explanationId);
        log.info("Submitting feedback for explanation {}", explanationId);

        QuestionExplanationRun explanationRun = explanationRunRepository.findById(explanationId)
                .orElseThrow(() -> new IllegalArgumentException("Explanation not found: " + explanationId));

        explanationRun.addFeedback(command.rating(), command.comment(), clock.now());
        explanationRunRepository.save(explanationRun);

        log.info("Feedback submitted for explanation {}: rating={}", explanationId, command.rating());
    }

    @Override
    public List<ExplanationResponse> getExplanationsForQuestions(List<UUID> uuids) {
        List<QuestionExplanationRun> runs = explanationRunRepository.findByQuestionIdsAndExamId(uuids);

        return runs.stream()
                .map(run -> new ExplanationResponse(
                        run.getId(),
                        run.getQuestionId(),
                        run.getContent(),
                        run.getModelName(),
                        run.getExpiresAt()
                ))
                .toList();
    }


    private void validateExplanationRequest(RequestExplanationCommand command, UUID userId) {
        Attempt attempt = attemptRepository.findById(command.examAttemptId())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + command.examAttemptId()));

        if (!attempt.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Attempt does not belong to user");
        }

        if (attempt.getStatus() != AttemptStatus.COMPLETED) {
            throw new IllegalStateException("Attempt must be COMPLETED to request explanations");
        }

        if (!attempt.getQuestionIds().contains(command.questionId())) {
            throw new IllegalArgumentException("Question does not belong to this attempt");
        }

        Question question = questionRepository.findById(command.questionId());
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + command.questionId());
        }
    }

    private String getSystemPrompt(String certification) {
        String year = new java.text.SimpleDateFormat("yyyy").format(new java.util.Date());
        return String.format("You are an AWS-certified solutions architect expert explaining exam questions for the %s certification in %s.", certification, year);
    }

    private String buildPrompt(Question question, String certification, String language) {
        List<String> correctList = question.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getOptionKey)
                .sorted(String::compareTo)
                .toList();

        String correctOption = String.join(", ", correctList);
        if (correctOption.isEmpty()) {
            throw new IllegalStateException("Question has no correct option defined: " + question.getId());
        }

        String optionsText = question.getOptions().stream()
                .sorted(Comparator.comparing(QuestionOption::getOptionKey))
                .map(opt -> opt.getOptionKey() + ") " + opt.getOptionText())
                .collect(Collectors.joining("\n"));

        return String.format("""
                You are explaining an AWS multiple-choice question.
                
                Task:
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
                """, language, question.getText(), optionsText, correctOption, certification);
    }

    private ExplanationResponse createExplanationResponse(
            String content,
            String modelName,
            RequestExplanationCommand command
    ) {
        Instant now = clock.now();
        Instant expiresAt = now.plus(EXPIRATION_DURATION);

        QuestionExplanationRun explanationRun = QuestionExplanationRun.create(
                command.questionId(),
                command.examAttemptId(),
                "openai", // TODO: make configurable
                modelName,
                PROMPT_VERSION,
                TEMPERATURE,
                command.language(),
                content,
                now,
                expiresAt
        );

        QuestionExplanationRun saved = explanationRunRepository.save(explanationRun);

        return new ExplanationResponse(
                saved.getId(),
                saved.getQuestionId(),
                saved.getContent(),
                saved.getModelName(),
                saved.getExpiresAt()
        );
    }
}

