package br.com.simulaaws.attempt.application.service;

import br.com.simulaaws.attempt.application.dto.AttemptResponse;
import br.com.simulaaws.attempt.application.mapper.AttemptMapper;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import br.com.simulaaws.attempt.application.port.out.AttemptRepositoryPort;
import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.clients.exam.ExamClient;
import br.com.simulaaws.clients.exam.QuestionClient;
import br.com.simulaaws.clients.exam.dto.QuestionResponse;
import br.com.simulaaws.common.ClockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static br.com.simulaaws.attempt.domain.AttemptStatus.IN_PROGRESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttemptService implements AttemptUseCase {

    private static final int MIN_QUESTION_COUNT = 10;
    private static final int MAX_QUESTION_COUNT = 65;

    private final AttemptRepositoryPort attemptRepository;
    private final QuestionClient questionClient;
    private final ExamClient examClient;
    private final ClockPort clock;
    private final AttemptMapper attemptMapper;

    @Override
    public AttemptResponse startAttempt(UUID userId, UUID examId, int questionCount) {
        log.debug("Attempt starting");

        log.debug("Validating input parameters");
        validateQuestionCount(questionCount);
        validateExamExists(examId);

        log.debug("Checking for existing attempt for user {} on exam {}", userId, examId);
        var existingAttempt = attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, IN_PROGRESS);

        if (existingAttempt.isPresent()) {
            log.debug("Attempt already exists, returning existing attempt: {}", existingAttempt.get().getId());
            return attemptMapper.toResponse(existingAttempt.get());
        }

        log.debug("Creating new attempt for user {} on exam {}", userId, examId);

        long seed = new Random().nextLong();
        List<UUID> selectedQuestionIds = selectQuestions(examId, questionCount, seed);
        Attempt attempt = Attempt.create(userId, examId, selectedQuestionIds, clock.now(), seed);
        attemptRepository.save(attempt);

        log.debug("New attempt created with id {}", attempt.getId());

        return attemptMapper.toResponse(attempt);
    }

    @Override
    public AttemptResponse finishAttempt(UUID attemptId, int score) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> {
                    log.warn("Attempt with id {} not found", attemptId);
                    return new IllegalArgumentException("Attempt not found: " + attemptId);
                });

        log.debug("Finishing attempt with id {}", attemptId);

        attempt.finish(score, clock.now());

        log.debug("Attempt with id {} finished with score {}", attemptId, score);
        attemptRepository.save(attempt);

        return attemptMapper.toResponse(attempt);
    }

    @Override
    public AttemptResponse getAttemptById(UUID attemptId) {
        return attemptRepository.findById(attemptId)
                .map(attemptMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
    }

    @Override
    public List<AttemptResponse> getAttemptsByUser(UUID userId) {
        log.debug("Getting all attempts for user {}", userId);
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(attemptMapper::toResponse)
                .toList();
    }

    private void validateQuestionCount(int questionCount) {
        if (questionCount < MIN_QUESTION_COUNT || questionCount > MAX_QUESTION_COUNT) {
            throw new IllegalArgumentException(
                    String.format("questionCount must be between %d and %d",
                            MIN_QUESTION_COUNT, MAX_QUESTION_COUNT)
            );
        }
    }

    private void validateExamExists(UUID examId) {
        if (!examClient.existsById(examId)) {
            throw new IllegalArgumentException("Exam not found: " + examId);
        }
    }

    private List<UUID> selectQuestions(UUID examId, int questionCount, long seed) {
        log.debug("Selecting {} questions for exam {} with seed {}", questionCount, examId, seed);

        var questionResponses = questionClient.findByExamId(examId);

        if (questionResponses.size() < questionCount) {
            String msg = String.format("Exam has only %d questions, cannot select %d", questionResponses.size(), questionCount);
            throw new IllegalStateException(msg);
        }

        log.debug("Total questions available for exam {}: {}", examId, questionResponses.size());

        var shuffled = new ArrayList<>(questionResponses);
        Collections.shuffle(shuffled, new Random(seed));

        return shuffled.stream()
                .limit(questionCount)
                .map(QuestionResponse::id)
                .toList();
    }
}
