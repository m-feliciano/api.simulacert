package com.simulacert.llm.application.service;

import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;
import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.RequestExplanationCommand;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.llm.application.port.in.QuestionExplanationUseCase;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.llm.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.llm.domain.QuestionExplanationRun;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionExplanationService implements QuestionExplanationUseCase {
    private static final String PROMPT_VERSION = "v1.0";
    private static final Double TEMPERATURE = 0.25;
    private static final Integer MAX_TOKENS = 400;
    private static final Duration EXPIRATION_DURATION = Duration.ofDays(7); // 1 week

    private final AttemptRepositoryPort attemptRepository;
    private final QuestionRepositoryPort questionRepository;
    private final QuestionExplanationRunRepositoryPort explanationRunRepository;
    private final ExplanationLLMPort llmProvider;
    private final ClockPort clock;
    private final ExplanationCacheService cacheService;

    @Override
    @Transactional
    public ExplanationResponse requestExplanation(RequestExplanationCommand command, UUID userId) {
        log.info("Requesting explanation for question {} by user {}", command.questionId(), userId);

        validateExplanationRequest(command, userId);
        // Limit user requests to prevent abuse
        validateUserRequestLimit(userId);

        Optional<QuestionExplanationRun> explanation = explanationRunRepository.findByQuestionIdAndLanguage(command.questionId(), command.language());
        if (explanation.isPresent()) {
            log.info("Returning existing explanation from DB for question {}", command.questionId());
            return new ExplanationResponse(
                    explanation.get().getId(),
                    explanation.get().getContent(),
                    explanation.get().getModelName(),
                    explanation.get().getExpiresAt()
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
    public void submitFeedback(UUID explanationId, SubmitFeedbackCommand command) {
        log.info("Submitting feedback for explanation {}", explanationId);

        QuestionExplanationRun explanationRun = explanationRunRepository.findById(explanationId)
                .orElseThrow(() -> new IllegalArgumentException("Explanation not found: " + explanationId));

        explanationRun.addFeedback(command.rating(), command.comment(), clock.now());
        explanationRunRepository.save(explanationRun);

        log.info("Feedback submitted for explanation {}: rating={}", explanationId, command.rating());
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
        return String.format("You are an AWS-certified solutions architect expert in %s.", certification);
    }

    private String buildPrompt(Question question, String certification, String language) {
        List<String> correctList = question.getOptions().stream()
                .filter(QuestionOption::getIsCorrect)
                .map(QuestionOption::getOptionKey)
                .sorted(String::compareTo)
                .collect(Collectors.toList());

        String correctOption = String.join(", ", correctList);
        if (correctOption.isEmpty()) {
            throw new IllegalStateException("Question has no correct option defined: " + question.getId());
        }

        String optionsText = question.getOptions().stream()
                .map(opt -> opt.getOptionKey() + ") " + opt.getOptionText())
                .collect(Collectors.joining("\n"));

        return String.format("""
                You are explaining an AWS certification multiple-choice question.
                
                Task:
                - Explain why each correct option is correct
                - Explain why each incorrect option is incorrect
                
                Rules (mandatory):
                - Use only AWS services, features, and behaviors documented by AWS
                - Do not introduce assumptions beyond the question context
                - Do not restate the question or options
                - Do not mention exams strategies or personal opinions
                - Do not mention that you are an AI
                - Write in %s
                - Cover ALL options listed
                - Be concise and technical (2–6 sentences per option)
                
                Output format (mandatory):
                - One line per option, in alphabetical order (A → Z)
                - Format exactly: Opção <OPTION_KEY>: <explanation>
                
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
                saved.getContent(),
                saved.getModelName(),
                saved.getExpiresAt()
        );
    }

    private void validateUserRequestLimit(UUID userId) {
        Integer requestCount = cacheService.getRequestCount(userId, 0);

        if (requestCount == null) requestCount = 0;

        int MAX_REQUESTS_PER_HOUR = 15;
        if (requestCount >= MAX_REQUESTS_PER_HOUR) {
            throw new IllegalStateException("User has exceeded the maximum number of explanation requests per hour");
        }

        cacheService.putRequestCount(userId, requestCount + 1);
    }
}

