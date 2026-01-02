package br.com.simulaaws.attempt.application.service;

import br.com.simulaaws.attempt.application.dto.SubmitAnswerRequest;
import br.com.simulaaws.attempt.application.port.in.AnswerUseCase;
import br.com.simulaaws.attempt.application.port.out.AnswerRepositoryPort;
import br.com.simulaaws.attempt.application.port.out.AttemptRepositoryPort;
import br.com.simulaaws.attempt.domain.Answer;
import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.domain.AttemptStatus;
import br.com.simulaaws.common.ClockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService implements AnswerUseCase {

    private final AnswerRepositoryPort answerRepository;
    private final AttemptRepositoryPort attemptRepository;
    private final ClockPort clock;

    @Override
    @Transactional
    public void submitAnswer(UUID attemptId, UUID questionId, SubmitAnswerRequest request) {
        log.info("Submitting answer for attempt {} question {}", attemptId, questionId);

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit answer to attempt with status: " + attempt.getStatus());
        }

        if (!attempt.getQuestionIds().contains(questionId)) {
            throw new IllegalArgumentException("Question not part of this attempt");
        }

        if (answerRepository.existsByAttemptIdAndQuestionId(attemptId, questionId)) {
            throw new IllegalStateException("Question already answered");
        }

        Answer answer = Answer.create(attemptId, questionId, request.selectedOption(), clock.now());
        answerRepository.save(answer);

        log.info("Answer submitted for attempt {} question {}", attemptId, questionId);
    }
}

