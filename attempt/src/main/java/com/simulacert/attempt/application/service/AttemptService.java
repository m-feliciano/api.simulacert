package com.simulacert.attempt.application.service;

import com.simulacert.attempt.application.dto.AttemptQuestionResponse;
import com.simulacert.attempt.application.dto.AttemptResponse;
import com.simulacert.attempt.application.dto.AttemptTimingResponse;
import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.dto.StartAttemptRequest;
import com.simulacert.attempt.application.port.in.AttemptUseCase;
import com.simulacert.attempt.application.port.out.AnswerRepositoryPort;
import com.simulacert.attempt.application.port.out.AttemptQueryPort;
import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Answer;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.Difficulty;
import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.dto.response.QuestionOptionDto;
import com.simulacert.exam.application.port.out.ExamQueryPort;
import com.simulacert.exam.application.port.out.QuestionOptionQueryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.service.XRayTracingService;
import com.simulacert.translation.application.service.TranslationService;
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
    private static final int MAX_QUESTION_COUNT = 65;

    private static final long MAX_ATTEMPT_DURATION_SECONDS = Duration.ofHours(4).toSeconds();
    private static final String EXAM_ID = "examId";
    private static final String ATTEMPT_ID = "attemptId";
    private static final String ATTEMPT_NOT_FOUND = "Attempt not found: ";

    private final Random random = new Random();

    private final AttemptRepositoryPort attemptRepository;
    private final AnswerRepositoryPort answerRepository;
    private final AttemptQueryPort attemptQueryPort;
    private final ExamQueryPort examQueryPort;
    private final QuestionRepositoryPort questionRepository;
    private final ClockPort clock;
    private final XRayTracingService xray;

    @Scheduled(cron = "0 0 0 * * ?") // runs every day at midnight
    public void cleanUpOldInProgressAttempts() {
        log.info("Starting cleanup of old in-progress attempts");

        var cutoff = clock.now().minus(Duration.ofDays(14));
        var oldAttempts = attemptRepository.findByStatusAndStartedAtBefore(IN_PROGRESS, cutoff);
        for (var attempt : oldAttempts) {
            log.info("Cancelling old in-progress attempt: {} which started at {}", attempt.getId(), attempt.getStartedAt());
            attempt.cancel(clock.now());
            attemptRepository.save(attempt);
        }
    }

    @Override
    @XRaySubsegment("attempt.startAttempt")
    public AttemptVo startAttempt(StartAttemptRequest request) {
        xray.putAnnotation(EXAM_ID, request.examId());

        validateQuestionCount(request.questionCount());
        validateExamExists(request.examId());

        UUID userId = request.userId();
        UUID examId = request.examId();

        var existingAttempt = attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, IN_PROGRESS);
        if (existingAttempt.isPresent()) {
            Attempt attempt = existingAttempt.get();
            xray.putAnnotation(ATTEMPT_ID, attempt.getId());

            Long remainingSeconds = attempt.getPausedRemainingSeconds();
            ensureTimerInitialized(attempt, remainingSeconds.intValue());
            return attempt.toVo();
        }

        List<UUID> selectedQuestionIds = selectQuestions(examId, request.questionCount(), random.nextLong(), request.difficulty());

        Attempt attempt = Attempt.create(
                userId,
                examId,
                selectedQuestionIds,
                clock.now(),
                random.nextLong(),
                request.mode()
        );

        xray.putAnnotation(ATTEMPT_ID, attempt.getId());

        ensureTimerInitialized(attempt, request.limitSeconds());
        attemptRepository.save(attempt);
        return attempt.toVo();
    }

    @Override
    @XRaySubsegment("attempt.pauseAttempt")
    public AttemptTimingResponse pauseAttempt(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        attempt.pause(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    @XRaySubsegment("attempt.resumeAttempt")
    public AttemptTimingResponse resumeAttempt(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        attempt.resume(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    @XRaySubsegment("attempt.heartbeatAttempt")
    public AttemptTimingResponse heartbeatAttempt(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        xray.putAnnotation(EXAM_ID, attempt.getExamId());

        attempt.heartbeat(clock.now());
        attemptRepository.save(attempt);
        return toTimingResponse(attempt, clock.now());
    }

    @Override
    @Transactional
    @XRaySubsegment("attempt.finishAttempt")
    public AttemptVo finishAttempt(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        xray.putAnnotation(EXAM_ID, attempt.getExamId());

        long totalQuestions = attempt.getQuestionIds().size();

        long correctAnswers = attemptQueryPort.countCorrectAnswers(attemptId);
        int score = (int) ((correctAnswers * 100) / totalQuestions);

        attempt.finish(score, clock.now());
        attemptRepository.save(attempt);

        return attempt.toVo();
    }

    @XRaySubsegment("attempt.cancelAttempt")
    public void cancelAttempt(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        log.info("Cancelling attempt {}", attemptId);

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));
        attempt.cancel(clock.now());
        attemptRepository.save(attempt);
    }

    @Override
    @XRaySubsegment("attempt.getAttemptById")
    public AttemptVo getAttemptById(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);
        return attemptRepository.findById(attemptId)
                .map(Attempt::toVo)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));
    }

    @Override
    @XRaySubsegment("attempt.getAttemptsByUser")
    public List<AttemptVo> getAttemptsByUser(UUID userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId)
                .stream()
                .map(Attempt::toVo)
                .toList();
    }

    @Override
    @XRaySubsegment("attempt.getAttemptQuestions")
    public List<AttemptQuestionResponse> getAttemptQuestions(UUID attemptId) {
        xray.putAnnotation(ATTEMPT_ID, attemptId);

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        xray.putAnnotation(EXAM_ID, attempt.getExamId());

        var answers = answerRepository.findByAttemptId(attemptId);

        var answerMap = answers.stream()
                .collect(Collectors.toMap(
                        Answer::getQuestionId,
                        Answer::getSelectedOption
                ));

        List<UUID> questionIds = attempt.getQuestionIds();
        var questions = questionRepository.findByExamId(attempt.getExamId())
                .stream()
                .filter(q -> questionIds.contains(q.getId()))
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<AttemptQuestionResponse> responses = questionIds
                .stream()
                .filter(questions::containsKey)
                .map(questions::get)
                .map(question -> {
                    var questionsDto = question.getOptions().stream()
                            .map(qo -> new QuestionOptionDto(
                                    qo.getOptionKey(),
                                    qo.getOptionText(),
                                    qo.getIsCorrect(),
                                    qo.getId()
                            ))
                            .toList();

                    return new AttemptQuestionResponse(
                            question.getId(),
                            question.getCode(),
                            question.getText(),
                            question.getDomain(),
                            question.getDifficulty(),
                            questionsDto,
                            answerMap.get(question.getId())
                    );
                })
                .toList();

        xray.putAnnotation("questionCount", responses.size());

        return responses;
    }

    @Override
    @XRaySubsegment("attempt.retakeAttempt")
    public AttemptResponse retakeAttempt(UUID attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException(ATTEMPT_NOT_FOUND + attemptId));

        if (attemptRepository.countByStatus(attempt.getUserId(), IN_PROGRESS) > 3) {
            throw new IllegalStateException("User has too many in-progress attempts");
        }

        Attempt newAttempt = Attempt.create(
                attempt.getUserId(),
                attempt.getExamId(),
                attempt.getQuestionIds(),
                clock.now(),
                random.nextLong(),
                attempt.getMode()
        );

        newAttempt.initTimer(MAX_ATTEMPT_DURATION_SECONDS);
        attemptRepository.save(newAttempt);
        return newAttempt.toResponse();
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

    private List<UUID> selectQuestions(UUID examId, int questionCount, long seed, String difficultyLevel) {
        log.debug("Selecting {} questions for exam {} with seed {}", questionCount, examId, seed);

        List<Question> allQuestions = questionRepository.findByExamId(examId);
        if (allQuestions.size() < questionCount) {
            throw new IllegalStateException("Exam has only " + allQuestions.size() + " questions");
        }

        List<UUID> selected;

        if (difficultyLevel == null || Difficulty.ANY.name().equalsIgnoreCase(difficultyLevel)) {
            int easyCount = (int) Math.round(questionCount * 0.3);
            int mediumCount = (int) Math.round(questionCount * 0.5);
            int hardCount = questionCount - easyCount - mediumCount;

            var easyQuestions = filterByDifficulty(allQuestions, Difficulty.EASY.name());
            var mediumQuestions = filterByDifficulty(allQuestions, Difficulty.MEDIUM.name());
            var hardQuestions = filterByDifficulty(allQuestions, Difficulty.HARD.name());

            if (hardQuestions.size() < hardCount) {
                mediumCount += (hardCount - hardQuestions.size());
                hardCount = hardQuestions.size();
            }

            if (mediumQuestions.size() < mediumCount) {
                easyCount += (mediumCount - mediumQuestions.size());
                mediumCount = mediumQuestions.size();
            }

            selected = new ArrayList<>();
            selected.addAll(selectRandom(easyQuestions, easyCount));
            selected.addAll(selectRandom(mediumQuestions, mediumCount));
            selected.addAll(selectRandom(hardQuestions, hardCount));

        } else {
            Difficulty difficulty = Difficulty.valueOf(difficultyLevel.toUpperCase());
            List<Question> candidates = new ArrayList<>(filterByDifficulty(allQuestions, difficulty.name()));

            if (candidates.size() < questionCount) {
                List<Difficulty> others = difficulty.getEasierThanThis();

                int i = others.size() - 1;
                while (i-- >= 0 && candidates.size() < questionCount) {
                    candidates.addAll(filterByDifficulty(allQuestions, others.get(i).name()));
                }
            }

            if (candidates.size() < questionCount) {
                for (Difficulty d : Difficulty.all()) {
                    if (d == difficulty) continue;

                    List<Question> questions = filterByDifficulty(allQuestions, d.name());
                    for (Question q : questions) {
                        if (candidates.stream().noneMatch(c -> c.getId().equals(q.getId()))) {
                            candidates.add(q);
                        }

                        if (candidates.size() >= questionCount) break;
                    }

                    if (candidates.size() >= questionCount) break;
                }
            }

            selected = selectRandom(candidates, questionCount);
        }

        Collections.shuffle(selected, random);
        return selected;
    }

    private List<Question> filterByDifficulty(List<Question> questions, String difficulty) {
        return questions.stream()
                .filter(q -> q.getDifficulty().equalsIgnoreCase(difficulty))
                .toList();
    }

    private List<UUID> selectRandom(List<Question> questions, int count) {
        if (questions.isEmpty()) {
            return new ArrayList<>();
        }

        if (questions.size() <= count) {
            return questions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());
        }

        var shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, random);
        return shuffled.stream()
                .limit(count)
                .map(Question::getId)
                .collect(Collectors.toList());
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
