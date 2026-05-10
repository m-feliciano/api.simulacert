package com.simulacert.exam.application.service;

import com.simulacert.common.ClockPort;
import com.simulacert.exam.application.dto.request.RequestExplanationCommand;
import com.simulacert.exam.application.mapper.QuestionMapper;
import com.simulacert.exam.application.port.out.QuestionExplanationRunRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.domain.QuestionExplanationRun;
import com.simulacert.exam.domain.QuestionOption;
import com.simulacert.llm.application.dto.ExplanationResponse;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.dto.SubmitFeedbackCommand;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.service.XRayTracingService;
import com.simulacert.translation.application.service.TranslationService;
import com.simulacert.util.UserContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionExplanationService Tests")
class QuestionExplanationServiceTest {

    @Mock
    private QuestionRepositoryPort questionRepository;

    @Mock
    private QuestionExplanationRunRepositoryPort explanationRunRepository;

    @Mock
    private ExplanationLLMPort llmProvider;

    @Mock
    private ClockPort clock;

    @Mock
    private XRayTracingService xray;

    @Mock
    private TranslationService translationService;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionExplanationService service;

    private UUID userId;
    private UUID questionId;
    private Question question;
    private RequestExplanationCommand command;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        questionId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();

        now = Instant.parse("2026-05-04T00:00:00Z");
        lenient().when(clock.now()).thenReturn(now);

        lenient().when(explanationRunRepository.findAllByQuestion(any(UUID.class)))
                .thenReturn(Optional.empty());

        question = Question.create(
                examId,
                "Which AWS service is best for serverless computing?",
                "Compute",
                "MEDIUM",
                "AWSCertPract_TEST"
        );

        questionId = question.getId();

        QuestionOption optionA = QuestionOption.create(question, "A", "EC2", false);
        QuestionOption optionB = QuestionOption.create(question, "B", "Lambda", true);
        question.getOptions().add(optionA);
        question.getOptions().add(optionB);

        command = new RequestExplanationCommand(
                questionId,
                attemptId,
                "AWS-SAA-C03"
        );

        lenient().when(questionRepository.findById(questionId)).thenReturn(question);

        lenient().when(translationService.getOrTranslate(anyString(), any(UUID.class), anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        lenient().when(questionMapper.toExplanationResponse(any(QuestionExplanationRun.class)))
                .thenAnswer(invocation -> {
                    QuestionExplanationRun run = invocation.getArgument(0);
                    return new ExplanationResponse(
                            run.getId(),
                            run.getQuestionId(),
                            run.getContent(),
                            run.getModelName(),
                            run.getExpiresAt()
                    );
                });
    }

    @Test
    @DisplayName("Should return cached explanation when repository already has it")
    void shouldReturnCachedExplanationWhenRepositoryHasIt() {
        // Given
        QuestionExplanationRun existing = QuestionExplanationRun.create(
                questionId, "openai", "cached-model", "v1.1",
                0.25, "pt", "cached explanation", now, now.plus(Duration.ofDays(30)),
                userId
        );
        when(explanationRunRepository.findAllByQuestion(questionId))
                .thenReturn(Optional.of(List.of(existing)));

        // When
        ExplanationResponse response = service.requestExplanation(command, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("cached explanation");
        assertThat(response.model()).isEqualTo("cached-model");
        verify(llmProvider, never()).generate(any(LLMRequest.class));
        verify(explanationRunRepository, never()).save(any(QuestionExplanationRun.class));
    }

    @Test
    @DisplayName("Should generate explanation successfully when repository miss")
    void shouldGenerateExplanationSuccessfullyWhenRepositoryMiss() {
        String generatedContent = "<div class=\"question-explanation\">explain</div>";
        when(llmProvider.generate(any(LLMRequest.class)))
                .thenReturn(new LLMResult(generatedContent, "gpt-4", "openai"));

        when(explanationRunRepository.save(any(QuestionExplanationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ExplanationResponse response = service.requestExplanation(command, userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo(generatedContent);
        assertThat(response.model()).isEqualTo("gpt-4");
        verify(llmProvider).generate(any(LLMRequest.class));
        verify(explanationRunRepository).save(any(QuestionExplanationRun.class));
    }

    @Test
    @DisplayName("Should throw exception when question has no correct option")
    void shouldThrowWhenQuestionHasNoCorrectOption() {
        // Given
        when(explanationRunRepository.findAllByQuestion(command.questionId()))
                .thenReturn(Optional.empty());

        RequestExplanationCommand localCommand = new RequestExplanationCommand(
                command.questionId(),
                command.examAttemptId(),
                command.certification()
        );

        Question q = Question.builder()
                .id(localCommand.questionId())
                .examId(question.getExamId())
                .code(question.getCode())
                .text(question.getText())
                .domain(question.getDomain())
                .language("pt_br")
                .difficulty(question.getDifficulty())
                .options(new java.util.ArrayList<>())
                .build();
        QuestionOption a = QuestionOption.create(q, "A", "EC2", false);
        QuestionOption b = QuestionOption.create(q, "B", "Lambda", false);
        q.getOptions().add(a);
        q.getOptions().add(b);

        when(questionRepository.findById(localCommand.questionId())).thenReturn(q);

        // When/Then
        assertThatThrownBy(() -> service.requestExplanation(localCommand, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Question has no correct option defined");

        verify(llmProvider, never()).generate(any(LLMRequest.class));
    }

    @Test
    @DisplayName("Should build correct prompt with question and options")
    void shouldBuildCorrectPromptWithQuestionAndOptions() {
        // Given
        when(explanationRunRepository.findAllByQuestion(questionId))
                .thenReturn(Optional.empty());

        when(llmProvider.generate(any(LLMRequest.class)))
                .thenReturn(new LLMResult("explanation", "gpt-4", "openai"));

        when(explanationRunRepository.save(any(QuestionExplanationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.requestExplanation(command, userId);

        // Then
        ArgumentCaptor<LLMRequest> requestCaptor = ArgumentCaptor.forClass(LLMRequest.class);
        verify(llmProvider).generate(requestCaptor.capture());
        LLMRequest captured = requestCaptor.getValue();

        assertThat(captured.systemPrompt()).contains("solutions architect");
        assertThat(captured.userPrompt()).contains("Which AWS service is best for serverless computing?");
        assertThat(captured.userPrompt()).contains("A) EC2");
        assertThat(captured.userPrompt()).contains("B) Lambda");
        assertThat(captured.userPrompt()).contains("pt");
        assertThat(captured.temperature()).isEqualTo(0.25);
        assertThat(captured.maxTokens()).isEqualTo(1200);
    }

    @Test
    @DisplayName("Should submit feedback successfully")
    void shouldSubmitFeedbackSuccessfully() {
        // Given
        UUID explanationId = UUID.randomUUID();
        QuestionExplanationRun run = QuestionExplanationRun.create(
                questionId, "openai", "gpt-4", "v1.1",
                0.25, "pt", "content", now, now.plus(Duration.ofDays(30)),
                userId
        );

        when(explanationRunRepository.findById(explanationId)).thenReturn(Optional.of(run));
        when(explanationRunRepository.save(any(QuestionExplanationRun.class))).thenReturn(run);

        SubmitFeedbackCommand feedback = new SubmitFeedbackCommand(5, "Excellent explanation!");

        UserContextHolder.setUser(userId);

        // When
        service.submitFeedback(explanationId, feedback);

        // Then
        verify(explanationRunRepository).findById(explanationId);
        verify(explanationRunRepository).save(run);
        assertThat(run.getUserFeedbacks().getFirst().getUserRating()).isEqualTo(5);
        assertThat(run.getUserFeedbacks().getLast().getFeedback()).isEqualTo("Excellent explanation!");
    }

    @Test
    @DisplayName("Should throw exception when submitting feedback for non-existent explanation")
    void shouldThrowExceptionWhenSubmittingFeedbackForNonExistentExplanation() {
        // Given
        UUID explanationId = UUID.randomUUID();
        when(explanationRunRepository.findById(explanationId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> service.submitFeedback(explanationId, new SubmitFeedbackCommand(5, "Great!")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Explanation not found");

        verify(explanationRunRepository).findById(explanationId);
        verify(explanationRunRepository, never()).save(any(QuestionExplanationRun.class));
    }

    @Test
    @DisplayName("Should persist explanation run with correct metadata")
    void shouldPersistExplanationRunWithCorrectMetadata() {
        // Given
        when(explanationRunRepository.findAllByQuestion(questionId))
                .thenReturn(Optional.empty());

        String generated = "Test explanation";
        when(llmProvider.generate(any(LLMRequest.class)))
                .thenReturn(new LLMResult(generated, "gpt-4-turbo", "openai"));

        when(explanationRunRepository.save(any(QuestionExplanationRun.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.requestExplanation(command, userId);

        // Then
        ArgumentCaptor<QuestionExplanationRun> runCaptor = ArgumentCaptor.forClass(QuestionExplanationRun.class);
        verify(explanationRunRepository).save(runCaptor.capture());
        QuestionExplanationRun saved = runCaptor.getValue();

        assertThat(saved.getQuestionId()).isEqualTo(questionId);
        assertThat(saved.getModelProvider()).isEqualTo("openai");
        assertThat(saved.getModelName()).isEqualTo("gpt-4-turbo");
        assertThat(saved.getPromptVersion()).isEqualTo("v1.1");
        assertThat(saved.getTemperature()).isEqualTo(0.25);
        assertThat(saved.getLanguage()).isEqualTo("pt_br");
        assertThat(saved.getContent()).isEqualTo(generated);
        assertThat(saved.getCreatedAt()).isEqualTo(now);
        assertThat(saved.getExpiresAt()).isEqualTo(now.plus(Duration.ofDays(30)));
    }
}

