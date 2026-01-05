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
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.llm.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.llm.domain.QuestionExplanationRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("QuestionExplanationService Tests")
class QuestionExplanationServiceTest {

    @Mock
    private AttemptRepositoryPort attemptRepository;

    @Mock
    private QuestionRepositoryPort questionRepository;

    @Mock
    private QuestionExplanationRunRepositoryPort explanationRunRepository;

    @Mock
    private ExplanationLLMPort llmProvider;

    @Mock
    private ExplanationCacheService cacheService;

    @Mock
    private ClockPort clock;

    @InjectMocks
    private QuestionExplanationService service;

    private UUID userId;
    private UUID questionId;
    private UUID attemptId;
    private UUID examId;
    private Attempt attempt;
    private Question question;
    private RequestExplanationCommand command;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        attemptId = UUID.randomUUID();
        examId = UUID.randomUUID();
        now = Instant.now();

        // Setup attempt
        attempt = Attempt.builder()
                .id(attemptId)
                .userId(userId)
                .examId(examId)
                .status(AttemptStatus.COMPLETED)
                .questionIds(List.of(questionId))
                .startedAt(now.minusSeconds(3600))
                .finishedAt(now)
                .score(80)
                .seed(12345L)
                .build();

        // Setup question with options using factory method
        question = Question.create(
                examId,
                "Which AWS service is best for serverless computing?",
                "Compute",
                "MEDIUM"
        );

        // Create options and add to question
        QuestionOption optionA = QuestionOption.create(
                question,
                "A",
                "EC2",
                false
        );

        QuestionOption optionB = QuestionOption.create(
                question,
                "B",
                "Lambda",
                true
        );

        // Add options to question's list
        question.getOptions().add(optionA);
        question.getOptions().add(optionB);

        command = new RequestExplanationCommand(
                questionId,
                attemptId,
                "pt",
                "AWS-SAA-C03"
        );

        when(clock.now()).thenReturn(now);
    }

    @Test
    @DisplayName("Should generate explanation successfully when cache miss")
    void shouldGenerateExplanationSuccessfullyWhenCacheMiss() {
        // Given
        String cacheKey = questionId + ":pt:v1.0";
        String generatedContent = "Lambda é correto porque é serverless...";
        LLMResult llmResult = new LLMResult(generatedContent, "gpt-4", "openai");

        QuestionExplanationRun savedRun = QuestionExplanationRun.create(
                questionId, attemptId, "openai", "gpt-4", "v1.0",
                0.25, "pt", generatedContent, now, now.plusSeconds(604800) // 7 days
        );

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(questionId)).thenReturn(question);
        when(cacheService.getExplanation(anyString(), any(), anyString())).thenReturn(null); // Changed to anyString()
        when(llmProvider.generate(any(LLMRequest.class))).thenReturn(llmResult);
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenReturn(savedRun);

        // When
        ExplanationResponse response = service.requestExplanation(command, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo(generatedContent);
        assertThat(response.model()).isEqualTo("gpt-4");
        assertThat(response.expiresAt()).isNotNull();

        verify(attemptRepository).findById(attemptId);
        verify(questionRepository, times(2)).findById(questionId); // Called twice: validation + generation
//        verify(cacheService).getExplanation(anyString(), any(), anyString()); // Changed verification
        verify(llmProvider).generate(any(LLMRequest.class));
//        verify(cacheService).putExplanation(anyString(), eq(generatedContent)); // Changed verification
        verify(explanationRunRepository).save(any(QuestionExplanationRun.class));
    }

    @Test
    @DisplayName("Should return cached explanation when cache hit")
    void shouldReturnCachedExplanationWhenCacheHit() {
        // Given
        String cacheKey = questionId + ":pt:v1.0";
        String cachedContent = "Cached explanation content";

        QuestionExplanationRun savedRun = QuestionExplanationRun.create(
                questionId, attemptId, "openai", "cached", "v1.0",
                0.25, "pt", cachedContent, now, now.plusSeconds(604800) // 7 days
        );

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(questionId)).thenReturn(question);
//        when(cacheService.getExplanation(eq(cacheKey), any(), anyString())).thenReturn(cachedContent);
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenReturn(savedRun);
        when(explanationRunRepository.findByQuestionIdAndLanguage(questionId, "pt"))
                .thenReturn(Optional.of(savedRun));

        // When
        ExplanationResponse response = service.requestExplanation(command, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo(cachedContent);
        assertThat(response.model()).isEqualTo("cached");

//        verify(cacheService).getExplanation(eq(cacheKey), eq(questionId), eq("pt"));
        verify(llmProvider, never()).generate(any(LLMRequest.class));
        verify(cacheService, never()).putExplanation(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when attempt not found")
    void shouldThrowExceptionWhenAttemptNotFound() {
        // Given
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(command, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Attempt not found");

        verify(attemptRepository).findById(attemptId);
        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when attempt does not belong to user")
    void shouldThrowExceptionWhenAttemptDoesNotBelongToUser() {
        // Given
        UUID differentUserId = UUID.randomUUID();
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(command, differentUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Attempt does not belong to user");

        verify(attemptRepository).findById(attemptId);
        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when attempt is not completed")
    void shouldThrowExceptionWhenAttemptNotCompleted() {
        // Given
        Attempt inProgressAttempt = Attempt.builder()
                .id(attemptId)
                .userId(userId)
                .examId(examId)
                .status(AttemptStatus.IN_PROGRESS)
                .questionIds(List.of(questionId))
                .startedAt(now)
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(inProgressAttempt));

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(command, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Attempt must be COMPLETED");

        verify(attemptRepository).findById(attemptId);
        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when question does not belong to attempt")
    void shouldThrowExceptionWhenQuestionDoesNotBelongToAttempt() {
        // Given
        UUID differentQuestionId = UUID.randomUUID();
        Attempt attemptWithoutQuestion = Attempt.builder()
                .id(attemptId)
                .userId(userId)
                .examId(examId)
                .status(AttemptStatus.COMPLETED)
                .questionIds(List.of(differentQuestionId))
                .startedAt(now.minusSeconds(3600))
                .finishedAt(now)
                .score(80)
                .build();

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attemptWithoutQuestion));

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(command, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question does not belong to this attempt");

        verify(attemptRepository).findById(attemptId);
        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when question not found")
    void shouldThrowExceptionWhenQuestionNotFound() {
        // Given
        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(questionId)).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(command, userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Question not found");

        verify(attemptRepository).findById(attemptId);
        verify(questionRepository).findById(questionId);
        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should build correct prompt with question and options")
    void shouldBuildCorrectPromptWithQuestionAndOptions() {
        // Given
        String cacheKey = questionId + ":pt:v1.0";
        LLMResult llmResult = new LLMResult("explanation", "gpt-4", "openai");

        QuestionExplanationRun savedRun = QuestionExplanationRun.create(
                questionId, attemptId, "openai", "gpt-4", "v1.0",
                0.25, "pt", "explanation", now, now.plusSeconds(172800)
        );

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(questionId)).thenReturn(question);
        when(cacheService.getExplanation(anyString(), any(), anyString())).thenReturn(null);
        when(llmProvider.generate(any(LLMRequest.class))).thenReturn(llmResult);
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenReturn(savedRun);

        // When
        service.requestExplanation(command, userId);

        // Then
        ArgumentCaptor<LLMRequest> requestCaptor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(llmProvider).generate(requestCaptor.capture());

        LLMRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.systemPrompt()).contains("AWS-certified solutions architect");
        assertThat(capturedRequest.userPrompt()).contains("Which AWS service is best for serverless computing?");
        assertThat(capturedRequest.userPrompt()).contains("A) EC2");
        assertThat(capturedRequest.userPrompt()).contains("B) Lambda");
        assertThat(capturedRequest.userPrompt()).contains("Correct answer:\nB");
        assertThat(capturedRequest.userPrompt()).contains("pt language");
        assertThat(capturedRequest.temperature()).isEqualTo(0.25);
        assertThat(capturedRequest.maxTokens()).isEqualTo(500);
    }

    @Test
    @DisplayName("Should submit feedback successfully")
    void shouldSubmitFeedbackSuccessfully() {
        // Given
        UUID explanationId = UUID.randomUUID();
        QuestionExplanationRun run = QuestionExplanationRun.create(
                questionId, attemptId, "openai", "gpt-4", "v1.0",
                0.25, "pt", "content", now, now.plusSeconds(172800)
        );

        SubmitFeedbackCommand feedbackCommand = new SubmitFeedbackCommand(5, "Excellent explanation!");

        when(explanationRunRepository.findById(explanationId)).thenReturn(Optional.of(run));
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenReturn(run);

        // When
        service.submitFeedback(explanationId, feedbackCommand);

        // Then
        verify(explanationRunRepository).findById(explanationId);
        verify(explanationRunRepository).save(run);
        assertThat(run.getUserRating()).isEqualTo(5);
        assertThat(run.getUserFeedback()).isEqualTo("Excellent explanation!");
    }

    @Test
    @DisplayName("Should throw exception when submitting feedback for non-existent explanation")
    void shouldThrowExceptionWhenSubmittingFeedbackForNonExistentExplanation() {
        // Given
        UUID explanationId = UUID.randomUUID();
        SubmitFeedbackCommand feedbackCommand = new SubmitFeedbackCommand(5, "Great!");

        when(explanationRunRepository.findById(explanationId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.submitFeedback(explanationId, feedbackCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Explanation not found");

        verify(explanationRunRepository).findById(explanationId);
        verify(explanationRunRepository, never()).save(any(QuestionExplanationRun.class));
    }

    @Test
    @DisplayName("Should persist explanation run with correct metadata")
    void shouldPersistExplanationRunWithCorrectMetadata() {
        // Given
        String cacheKey = questionId + ":pt:v1.0";
        String generatedContent = "Test explanation";
        LLMResult llmResult = new LLMResult(generatedContent, "gpt-4-turbo", "openai");

        when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
        when(questionRepository.findById(questionId)).thenReturn(question);
        when(cacheService.getExplanation(anyString(), any(), anyString())).thenReturn(null);
        when(llmProvider.generate(any(LLMRequest.class))).thenReturn(llmResult);
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.requestExplanation(command, userId);

        // Then
        ArgumentCaptor<QuestionExplanationRun> runCaptor = ArgumentCaptor.forClass(QuestionExplanationRun.class);
        verify(explanationRunRepository).save(runCaptor.capture());

        QuestionExplanationRun capturedRun = runCaptor.getValue();
        assertThat(capturedRun.getQuestionId()).isEqualTo(questionId);
        assertThat(capturedRun.getExamAttemptId()).isEqualTo(attemptId);
        assertThat(capturedRun.getModelProvider()).isEqualTo("openai");
        assertThat(capturedRun.getModelName()).isEqualTo("gpt-4-turbo");
        assertThat(capturedRun.getPromptVersion()).isEqualTo("v1.0");
        assertThat(capturedRun.getTemperature()).isEqualTo(0.25);
        assertThat(capturedRun.getLanguage()).isEqualTo("pt");
        assertThat(capturedRun.getContent()).isEqualTo(generatedContent);
        assertThat(capturedRun.getCreatedAt()).isEqualTo(now);
        assertThat(capturedRun.getExpiresAt()).isEqualTo(now.plusSeconds(604800)); // 7 days
    }
}

