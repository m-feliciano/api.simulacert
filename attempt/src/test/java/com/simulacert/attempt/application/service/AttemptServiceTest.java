package com.simulacert.attempt.application.service;

import com.simulacert.attempt.application.dto.AttemptVo;
import com.simulacert.attempt.application.port.out.AnswerRepositoryPort;
import com.simulacert.attempt.application.port.out.AttemptQueryPort;
import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Answer;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.attempt.domain.AttemptStatus;
import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.port.out.ExamQueryPort;
import com.simulacert.exam.application.port.out.QuestionOptionQueryPort;
import com.simulacert.exam.application.port.out.QuestionQueryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttemptService Tests")
class AttemptServiceTest {

    @Mock
    private AttemptRepositoryPort attemptRepository;

    @Mock
    private AnswerRepositoryPort answerRepository;

    @Mock
    private AttemptQueryPort attemptQueryPort;

    @Mock
    private ExamQueryPort examQueryPort;

    @Mock
    private QuestionQueryPort questionQueryPort;

    @Mock
    private QuestionRepositoryPort questionRepository;

    @Mock
    private QuestionOptionQueryPort questionOptionQueryPort;

    @Mock
    private ClockPort clock;

    @InjectMocks
    private AttemptService attemptService;

    private UUID userId;
    private UUID examId;
    private UUID attemptId;
    private Instant now;
    private Attempt testAttempt;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        examId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        now = Instant.parse("2026-01-04T10:00:00Z");

        List<UUID> questionIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        testAttempt = Attempt.create(userId, examId, questionIds, now, 12345L);
    }

    @Test
    @DisplayName("Should start attempt successfully")
    void shouldStartAttemptSuccessfully() {
        int questionCount = 15;
        List<Question> mockQuestions = createMockQuestions(examId, 50);

        when(examQueryPort.existsById(examId)).thenReturn(true);
        when(attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(questionRepository.findByExamId(examId)).thenReturn(mockQuestions);
        when(clock.now()).thenReturn(now);
        when(attemptRepository.save(any(Attempt.class))).thenReturn(testAttempt);

        AttemptVo result = attemptService.startAttempt(userId, examId, questionCount);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.examId()).isEqualTo(examId);
        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.questionIds()).isNotNull();
        verify(examQueryPort).existsById(examId);
        verify(questionRepository).findByExamId(examId);
        verify(attemptRepository).save(any(Attempt.class));
    }

    @Test
    @DisplayName("Should return existing attempt if already in progress")
    void shouldReturnExistingAttemptIfAlreadyInProgress() {
        int questionCount = 15;

        when(examQueryPort.existsById(examId)).thenReturn(true);
        when(attemptRepository.findByUserIdAndExamIdAndStatus(userId, examId, AttemptStatus.IN_PROGRESS))
                .thenReturn(Optional.of(testAttempt));

        AttemptVo result = attemptService.startAttempt(userId, examId, questionCount);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.examId()).isEqualTo(examId);
        verify(examQueryPort).existsById(examId);
        verify(attemptRepository).findByUserIdAndExamIdAndStatus(userId, examId, AttemptStatus.IN_PROGRESS);
        verify(attemptRepository, never()).save(any(Attempt.class));
    }

    @Test
    @DisplayName("Should throw exception when question count is below minimum")
    void shouldThrowExceptionWhenQuestionCountIsBelowMinimum() {
        int questionCount = 5;

        assertThatThrownBy(() -> attemptService.startAttempt(userId, examId, questionCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("questionCount must be between");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when question count is above maximum")
    void shouldThrowExceptionWhenQuestionCountIsAboveMaximum() {
        int questionCount = 150;

        assertThatThrownBy(() -> attemptService.startAttempt(userId, examId, questionCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("questionCount must be between");

        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when exam does not exist")
    void shouldThrowExceptionWhenExamDoesNotExist() {
        int questionCount = 15;

        when(examQueryPort.existsById(examId)).thenReturn(false);

        assertThatThrownBy(() -> attemptService.startAttempt(userId, examId, questionCount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exam not found: " + examId);

        verify(examQueryPort).existsById(examId);
        verify(attemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should finish attempt successfully")
    void shouldFinishAttemptSuccessfully() {
        List<Answer> answers = createMockAnswers(attemptId, testAttempt.getQuestionIds());

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));
        when(answerRepository.findByAttemptId(attemptId)).thenReturn(answers);
        when(attemptQueryPort.countCorrectAnswers(attemptId)).thenReturn(7L);
        when(clock.now()).thenReturn(now.plusSeconds(1800));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttemptVo result = attemptService.finishAttempt(attemptId);

        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.score()).isEqualTo(70);
        verify(attemptRepository).findById(attemptId);
        verify(answerRepository).findByAttemptId(attemptId);
        verify(attemptQueryPort).countCorrectAnswers(attemptId);
        verify(attemptRepository).save(any(Attempt.class));
    }

    @Test
    @DisplayName("Should throw exception when attempting to finish non-existent attempt")
    void shouldThrowExceptionWhenFinishingNonExistentAttempt() {
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attemptService.finishAttempt(attemptId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attempt not found: " + attemptId);

        verify(attemptRepository).findById(attemptId);
    }

    @Test
    @DisplayName("Should get attempt by ID")
    void shouldGetAttemptById() {
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));

        AttemptVo result = attemptService.getAttemptById(attemptId);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.examId()).isEqualTo(examId);
        verify(attemptRepository).findById(attemptId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent attempt")
    void shouldThrowExceptionWhenGettingNonExistentAttempt() {
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attemptService.getAttemptById(attemptId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attempt not found: " + attemptId);

        verify(attemptRepository).findById(attemptId);
    }

    @Test
    @DisplayName("Should get attempts by user ID")
    void shouldGetAttemptsByUserId() {
        Attempt attempt2 = Attempt.create(userId, examId, testAttempt.getQuestionIds(), now, 54321L);

        when(attemptRepository.findByUserIdOrderByStartedAtDesc(userId))
                .thenReturn(List.of(testAttempt, attempt2));

        List<AttemptVo> results = attemptService.getAttemptsByUser(userId);

        assertThat(results).hasSize(2);
        verify(attemptRepository).findByUserIdOrderByStartedAtDesc(userId);
    }

    private List<Question> createMockQuestions(UUID examId, int count) {
        List<Question> questions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Question q = Question.create(examId, "Question " + i, "AWS", i % 3 == 0 ? "EASY" : i % 3 == 1 ? "MEDIUM" : "HARD");
            questions.add(q);
        }
        return questions;
    }

    private List<Answer> createMockAnswers(UUID attemptId, List<UUID> questionIds) {
        List<Answer> answers = new ArrayList<>();
        for (UUID questionId : questionIds) {
            Answer answer = Answer.create(attemptId, questionId, "A", now);
            answers.add(answer);
        }
        return answers;
    }
}

