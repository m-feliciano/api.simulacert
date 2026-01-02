package br.com.simulaaws.attempt.application.service;

import br.com.simulaaws.attempt.application.dto.AttemptVo;
import br.com.simulaaws.attempt.application.port.in.AttemptUseCase;
import br.com.simulaaws.attempt.application.port.out.AttemptRepositoryPort;
import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.common.ClockPort;
import br.com.simulaaws.exam.application.port.out.ExamQueryPort;
import br.com.simulaaws.exam.application.port.out.QuestionQueryPort;
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
    private final ExamQueryPort examQueryPort;
    private final QuestionQueryPort questionQueryPort;
    private final ClockPort clock;

    @Override
    public AttemptVo startAttempt(UUID userId, UUID examId, int questionCount) {
        validateQuestionCount(questionCount);
        validateExamExists(examId);

        var existingAttempt = attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, IN_PROGRESS);
        if (existingAttempt.isPresent()) {
            return existingAttempt.get().toVo();
        }

        long seed = new Random().nextLong();
        List<UUID> selectedQuestionIds = selectQuestions(examId, questionCount, seed);

        Attempt attempt = Attempt.create(
                userId,
                examId,
                selectedQuestionIds,
                clock.now(),
                seed
        );

        attemptRepository.save(attempt);
        return attempt.toVo();
    }

    @Override
    public AttemptVo finishAttempt(UUID attemptId, int score) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        attempt.finish(score, clock.now());
        attemptRepository.save(attempt);

        return attempt.toVo();
    }

    @Override
    public AttemptVo getAttemptById(UUID attemptId) {
        return attemptRepository.findById(attemptId)
                .map(Attempt::toVo)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
    }

    @Override
    public List<AttemptVo> getAttemptsByUser(UUID userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(Attempt::toVo)
                .toList();
    }

    private void validateQuestionCount(int questionCount) {
        if (questionCount < MIN_QUESTION_COUNT || questionCount > MAX_QUESTION_COUNT) {
            throw new IllegalArgumentException(
                    "questionCount must be between " + MIN_QUESTION_COUNT + " and " + MAX_QUESTION_COUNT
            );
        }
    }

    private void validateExamExists(UUID examId) {
        if (!examQueryPort.existsById(examId)) {
            throw new IllegalArgumentException("Exam not found: " + examId);
        }
    }

    private List<UUID> selectQuestions(UUID examId, int questionCount, long seed) {
        log.debug("Selecting {} questions for exam {} with seed {}", questionCount, examId, seed);

        var questionIds = questionQueryPort.findQuestionIdsByExamId(examId);
        if (questionIds.size() < questionCount) {
            throw new IllegalStateException("Exam has only " + questionIds.size() + " questions");
        }

        var shuffled = new ArrayList<>(questionIds);
        Collections.shuffle(shuffled, new Random(seed));

        return shuffled.stream()
                .limit(questionCount)
                .toList();
    }
}
