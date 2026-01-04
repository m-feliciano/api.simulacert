package com.simulacert.attempt.application.service;

import com.simulacert.attempt.application.dto.SubmitAnswerRequest;
import com.simulacert.attempt.application.port.out.AnswerRepositoryPort;
import com.simulacert.attempt.application.port.out.AttemptRepositoryPort;
import com.simulacert.attempt.domain.Answer;
import com.simulacert.attempt.domain.Attempt;
import com.simulacert.common.ClockPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnswerService Tests")
class AnswerServiceTest {

    @Mock
    private AnswerRepositoryPort answerRepository;

    @Mock
    private AttemptRepositoryPort attemptRepository;

    @Mock
    private ClockPort clock;

    @InjectMocks
    private AnswerService answerService;

    private UUID attemptId;
    private UUID questionId;
    private Instant now;
    private Attempt testAttempt;

    @BeforeEach
    void setUp() {
        questionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        now = Instant.parse("2026-01-04T10:00:00Z");

        List<UUID> questionIds = List.of(questionId, UUID.randomUUID(), UUID.randomUUID());
        testAttempt = Attempt.create(userId, examId, questionIds, now, 12345L);
        attemptId = testAttempt.getId();
    }

    @Test
    @DisplayName("Should submit answer successfully")
    void shouldSubmitAnswerSuccessfully() {
        SubmitAnswerRequest request = new SubmitAnswerRequest("B");

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));
        when(answerRepository.existsByAttemptIdAndQuestionId(attemptId, questionId)).thenReturn(false);
        when(clock.now()).thenReturn(now);
        when(answerRepository.save(any(Answer.class))).thenReturn(Answer.create(attemptId, questionId, "B", now));

        answerService.submitAnswer(attemptId, questionId, request);

        verify(attemptRepository).findById(attemptId);
        verify(answerRepository).existsByAttemptIdAndQuestionId(attemptId, questionId);
        verify(answerRepository).save(any(Answer.class));
    }

    @Test
    @DisplayName("Should throw exception when attempt not found")
    void shouldThrowExceptionWhenAttemptNotFound() {
        SubmitAnswerRequest request = new SubmitAnswerRequest("A");

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.submitAnswer(attemptId, questionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attempt not found: " + attemptId);

        verify(attemptRepository).findById(attemptId);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when attempt is not in progress")
    void shouldThrowExceptionWhenAttemptIsNotInProgress() {
        SubmitAnswerRequest request = new SubmitAnswerRequest("A");
        testAttempt.finish(80, now.plusSeconds(1800));

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));

        assertThatThrownBy(() -> answerService.submitAnswer(attemptId, questionId, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit answer to attempt with status");

        verify(attemptRepository).findById(attemptId);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when question is not part of attempt")
    void shouldThrowExceptionWhenQuestionIsNotPartOfAttempt() {
        SubmitAnswerRequest request = new SubmitAnswerRequest("A");
        UUID wrongQuestionId = UUID.randomUUID();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));

        assertThatThrownBy(() -> answerService.submitAnswer(attemptId, wrongQuestionId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Question not part of this attempt");

        verify(attemptRepository).findById(attemptId);
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should replace answer when already submitted")
    void shouldReplaceAnswerWhenAlreadySubmitted() {
        SubmitAnswerRequest request = new SubmitAnswerRequest("A");

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(testAttempt));
        when(answerRepository.existsByAttemptIdAndQuestionId(attemptId, questionId)).thenReturn(true);
        when(clock.now()).thenReturn(now);
        when(answerRepository.save(any(Answer.class))).thenReturn(Answer.create(attemptId, questionId, "A", now));

        answerService.submitAnswer(attemptId, questionId, request);

        verify(answerRepository).existsByAttemptIdAndQuestionId(attemptId, questionId);
        verify(answerRepository).deleteByAttemptIdAndQuestionId(attemptId, questionId);
        verify(answerRepository).save(any(Answer.class));
    }
}

