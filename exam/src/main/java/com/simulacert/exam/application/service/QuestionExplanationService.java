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
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.PromptRequest;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.service.TracingService;
import com.simulacert.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionExplanationService implements QuestionExplanationUseCase {
    private static final String PROMPT_VERSION = "2";
    private static final Double TEMPERATURE = 0.25;
    private static final int MAX_OUTPUT_TOKENS = 2000;
    private static final Duration EXPIRATION_DURATION = Duration.ofDays(60);
    private static final String PROVIDER = "AWS";

    private final QuestionRepositoryPort questionRepository;
    private final QuestionExplanationRunRepositoryPort explanationRunRepository;
    private final ExplanationLLMPort llmProvider;
    private final ClockPort clock;
    private final TracingService xray;
    private final QuestionMapper questionMapper;

    @Value("${app.llm.openai.prompt-id}")
    private String promptId;

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

        if (explanationRunRepository.countExplanationsByUserIdToday(userId) >= 5) {
            throw new IllegalStateException("You have reached the daily limit of 5 explanations. Please try again tomorrow.");
        }

        Question question = questionRepository.findById(questionId);
        // TODO: Use a dynamic provider below
        PromptRequest llmRequest = buildLLMRequest(question, command.certification());
        LLMResult llmResult = llmProvider.generate(llmRequest, MAX_OUTPUT_TOKENS);

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


    private PromptRequest buildLLMRequest(Question question, String certification) {
        Objects.requireNonNull(promptId, "Prompt ID cannot be null");
        Objects.requireNonNull(question, "Question cannot be null");
        Objects.requireNonNull(question.getOptions(), "Question options cannot be null");

        List<String> correctList = question.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getOptionKey)
                .sorted(String::compareTo)
                .toList();

        if (correctList.isEmpty()) {
            throw new IllegalStateException("Question has no correct option defined: " + question.getId());
        }

        String optionsText = question.getOptions().stream()
                .sorted(Comparator.comparing(QuestionOption::getOptionKey))
                .map(opt -> opt.getOptionKey() + ") " + opt.getOptionText())
                .collect(Collectors.joining("\n"));

        String correctAnswers = String.join(", ", correctList);

        return PromptRequest.builder()
                .prompt(new PromptRequest.Prompt(promptId, PROMPT_VERSION))
                .variables(new PromptRequest.Variables(
                        question.getLanguage(),
                        PROVIDER,
                        question.getText(),
                        optionsText,
                        correctAnswers,
                        certification
                ))
                .build();
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
                promptId + "-v" + PROMPT_VERSION,
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

