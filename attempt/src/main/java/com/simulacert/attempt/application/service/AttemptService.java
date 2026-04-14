package com.simulacert.attempt.application.service;

import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptTimingResponse;
import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.dto.QuestionOption;
import com.simulacert.attempt.application.port.in.AttemptUseCase;
import com.simulacert.attempt.application.port.out.AnswerRepositoryPort;
import com.simulacert.attempt.application.port.out.AttemptQueryPort;
import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Answer;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.port.out.ExamQueryPort;
import com.simulacert.exam.application.port.out.QuestionOptionQueryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.simulacert.attempt.domain.AttemptStatus.IN_PROGRESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttemptService implements AttemptUseCase {

    private static final int MIN_QUESTION_COUNT = 10;
    private static final int MAX_QUESTION_COUNT = 100;

    private static final long MAX_ATTEMPT_DURATION_SECONDS = Duration.ofHours(4).toSeconds();

    private final AttemptRepositoryPort attemptRepository;
    private final AnswerRepositoryPort answerRepository;
    private final AttemptQueryPort attemptQueryPort;
    private final ExamQueryPort examQueryPort;
    private final QuestionRepositoryPort questionRepository;
    private final QuestionOptionQueryPort questionOptionQueryPort;
    private final ClockPort clock;

    @Scheduled(cron = "0 0 * * * ?") // runs every hour
    public void cleanUpOldInProgressAttempts() {
        log.info("Starting cleanup of old in-progress attempts");

        var cutoff = clock.now().minusSeconds(Duration.ofDays(7).toSeconds());
        var oldAttempts = attemptRepository.findByStatusAndStartedAtBefore(IN_PROGRESS, cutoff);
        for (var attempt : oldAttempts) {
            log.info("Cancelling old in-progress attempt: {} which started at {}", attempt.getId(), attempt.getStartedAt());
            attempt.cancel(clock.now());
            attemptRepository.save(attempt);
        }
    }

    @Override
    @XRaySubsegment(value = "attempt.startAttempt", captureArgs = true)
    public AttemptVo startAttempt(UUID userId, UUID examId, int questionCount, Integer limitSeconds) {
        validateQuestionCount(questionCount);
        validateExamExists(examId);

        var existingAttempt = attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, IN_PROGRESS);
        if (existingAttempt.isPresent()) {
            Attempt attempt = existingAttempt.get();
            Long remainingSeconds = attempt.getPausedRemainingSeconds();
            ensureTimerInitialized(attempt, remainingSeconds.intValue());
            return attempt.toVo();
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

        ensureTimerInitialized(attempt, limitSeconds);
        attemptRepository.save(attempt);
        return attempt.toVo();
    }

    @Override
    @XRaySubsegment(value = "attempt.pauseAttempt", captureArgs = true)
    public AttemptTimingResponse pauseAttempt(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        attempt.pause(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    @XRaySubsegment(value = "attempt.resumeAttempt", captureArgs = true)
    public AttemptTimingResponse resumeAttempt(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        attempt.resume(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    public AttemptTimingResponse heartbeatAttempt(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        attempt.heartbeat(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    @Transactional
    @XRaySubsegment(value = "attempt.finishAttempt", captureArgs = true)
    public AttemptVo finishAttempt(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        long totalQuestions = attempt.getQuestionIds().size();

        long correctAnswers = attemptQueryPort.countCorrectAnswers(attemptId);
        int score = (int) ((correctAnswers * 100) / totalQuestions);

        attempt.finish(score, clock.now());
        attemptRepository.save(attempt);

        return attempt.toVo();
    }

    @XRaySubsegment(value = "attempt.cancelAttempt", captureArgs = true)
    public void cancelAttempt(UUID attemptId) {
        log.info("Cancelling attempt {}", attemptId);

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        attempt.cancel(clock.now());
        attemptRepository.save(attempt);
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

    @Override
    @XRaySubsegment(value = "attempt.getAttemptQuestions", captureArgs = true)
    public List<AttemptQuestionResponse> getAttemptQuestions(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));

        var answers = answerRepository.findByAttemptId(attemptId);
        var answerMap = answers.stream()
                .collect(Collectors.toMap(
                        Answer::getQuestionId,
                        Answer::getSelectedOption
                ));

        return attempt.getQuestionIds()
                .stream()
                .map(questionId -> {
                    var question = questionRepository.findById(questionId);
                    if (question == null) {
                        throw new IllegalStateException("Question not found: " + questionId);
                    }

                    var questionOptions = questionOptionQueryPort.findByQuestionId(questionId);
                    var options = questionOptions.stream()
                            .map(qo -> new QuestionOption(qo.key(), qo.text(), qo.isCorrect()))
                            .toList();

                    String selectedOption = answerMap.get(questionId);

                    return new AttemptQuestionResponse(
                            question.getId(),
                            question.getText(),
                            question.getDomain(),
                            question.getDifficulty(),
                            options,
                            selectedOption
                    );
                })
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

        List<Question> allQuestions = questionRepository.findByExamId(examId);
        if (allQuestions.size() < questionCount) {
            throw new IllegalStateException("Exam has only " + allQuestions.size() + " questions");
        }

        // Embaralha todas as perguntas com a semente fornecida
        List<Question> shuffledQuestions = new ArrayList<>(allQuestions);
        Collections.shuffle(shuffledQuestions, new Random(seed));

        // Distribuição por dificuldade: 30% easy, 50% medium, 20% hard
        int easyCount = (int) Math.round(questionCount * 0.3);
        int mediumCount = (int) Math.round(questionCount * 0.5);
        int hardCount = questionCount - easyCount - mediumCount; // garante soma exata

        // Se não houver perguntas suficientes em uma categoria, redistribui para as outras
        var hardQuestions = filterByDifficulty(shuffledQuestions, "HARD");
        if (hardQuestions.size() < hardCount) {
            int deficit = hardCount - hardQuestions.size();
            hardCount = hardQuestions.size();
            mediumCount += deficit;
        }

        var mediumQuestions = filterByDifficulty(shuffledQuestions, "MEDIUM");
        if (mediumQuestions.size() < mediumCount) {
            int deficit = mediumCount - mediumQuestions.size();
            mediumCount = mediumQuestions.size();
            easyCount += deficit;
        }

        var easyQuestions = filterByDifficulty(shuffledQuestions, "EASY");
        if (easyQuestions.size() < easyCount) {
            throw new IllegalStateException("Not enough questions to satisfy the difficulty distribution");
        }

        Random random = new Random(seed);

        List<UUID> selected = new ArrayList<>();
        selected.addAll(selectRandom(easyQuestions, easyCount, random));
        selected.addAll(selectRandom(mediumQuestions, mediumCount, random));
        selected.addAll(selectRandom(hardQuestions, hardCount, random));

        Collections.shuffle(selected, random);

        return selected;
    }

    private List<Question> filterByDifficulty(List<Question> questions, String difficulty) {
        return questions.stream()
                .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                .toList();
    }

    private List<UUID> selectRandom(List<Question> questions, int count, Random random) {
        var shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, random);
        return shuffled.stream()
                .limit(count)
                .map(Question::getId)
                .toList();
    }

    private void ensureTimerInitialized(Attempt attempt, Integer limitSeconds) {
        if (limitSeconds == null || limitSeconds <= 0) {
            throw new IllegalArgumentException("limitSeconds must be provided and > 0");
        }

        long clamped = Math.min(limitSeconds.longValue(), MAX_ATTEMPT_DURATION_SECONDS);
        attempt.initTimer(clamped);
    }

    private AttemptTimingResponse toTimingResponse(Attempt attempt, Instant now) {
        Instant endsAt = attempt.getEndsAt();
        Instant pausedAt = attempt.getPausedAt();

        return new AttemptTimingResponse(
                endsAt != null ? endsAt.toString() : null,
                attempt.remainingSeconds(now),
                attempt.isPaused(),
                pausedAt != null ? pausedAt.toString() : null
        );
    }
}
