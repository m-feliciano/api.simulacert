package com.simulacert.exam.application.service;

import com.simulacert.exam.application.dto.request.CreateQuestionRequest;
import com.simulacert.exam.application.dto.request.QuestionOptionDto;
import com.simulacert.exam.application.dto.response.QuestionResponse;
import com.simulacert.exam.application.mapper.QuestionMapper;
import com.simulacert.exam.application.port.out.ExamRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService Tests")
class QuestionServiceTest {

    @Mock
    private QuestionRepositoryPort questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private ExamRepositoryPort examRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private UUID examId;
    private UUID questionId;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        examId = UUID.randomUUID();
        testQuestion = Question.create(examId, "Test Question?", "AWS", "MEDIUM");
        questionId = testQuestion.getId();
    }

    @Test
    @DisplayName("Should create question successfully")
    void shouldCreateQuestionSuccessfully() {
        List<QuestionOptionDto> options = List.of(
                new QuestionOptionDto("A", "Option A", false),
                new QuestionOptionDto("B", "Option B", true)
        );

        CreateQuestionRequest request = new CreateQuestionRequest(
                examId,
                "New Question?",
                "AWS",
                "EASY",
                options
        );

        QuestionResponse expectedResponse = new QuestionResponse(
                questionId,
                examId,
                "New Question?",
                "AWS",
                "EASY",
                List.of()
        );

        when(examRepository.existsById(examId)).thenReturn(true);
        when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
        when(questionOptionRepository.saveAll(anyList())).thenReturn(List.of());
        when(questionMapper.toResponse(testQuestion)).thenReturn(expectedResponse);

        QuestionResponse response = questionService.createQuestion(request);

        assertThat(response).isNotNull();
        assertThat(response.text()).isEqualTo("New Question?");
        verify(examRepository).existsById(examId);
        verify(questionRepository).save(any(Question.class));
        verify(questionOptionRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when creating question for non-existent exam")
    void shouldThrowExceptionWhenCreatingQuestionForNonExistentExam() {
        CreateQuestionRequest request = new CreateQuestionRequest(
                examId,
                "Question?",
                "AWS",
                "EASY",
                List.of(new QuestionOptionDto("A", "Option A", true))
        );

        when(examRepository.existsById(examId)).thenReturn(false);

        assertThatThrownBy(() -> questionService.createQuestion(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exam not found: " + examId);

        verify(examRepository).existsById(examId);
        verify(questionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get questions by exam ID")
    void shouldGetQuestionsByExamId() {
        Question question2 = Question.create(examId, "Question 2?", "AWS", "HARD");

        when(questionRepository.findByExamId(examId)).thenReturn(List.of(testQuestion, question2));
        when(questionMapper.toResponseList(anyList())).thenReturn(List.of(
                new QuestionResponse(questionId, examId, "Test Question?", "AWS", "MEDIUM", List.of()),
                new QuestionResponse(question2.getId(), examId, "Question 2?", "AWS", "HARD", List.of())
        ));

        List<QuestionResponse> responses = questionService.getQuestionsByExamId(examId);

        assertThat(responses).hasSize(2);
        verify(questionRepository).findByExamId(examId);
    }

    @Test
    @DisplayName("Should get questions by exam ID with pagination")
    void shouldGetQuestionsByExamIdWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> questionPage = new PageImpl<>(List.of(testQuestion), pageable, 1);

        when(questionRepository.findByExamIdPaginated(examId, pageable)).thenReturn(questionPage);
        when(questionMapper.toResponse(testQuestion)).thenReturn(
                new QuestionResponse(questionId, examId, "Test Question?", "AWS", "MEDIUM", List.of())
        );

        Page<QuestionResponse> responses = questionService.getQuestionsByExamIdPaginated(examId, pageable);

        assertThat(responses).hasSize(1);
        assertThat(responses.getTotalElements()).isEqualTo(1);
        verify(questionRepository).findByExamIdPaginated(examId, pageable);
    }

    @Test
    @DisplayName("Should get question by ID")
    void shouldGetQuestionById() {
        QuestionResponse expectedResponse = new QuestionResponse(
                questionId,
                examId,
                "Test Question?",
                "AWS",
                "MEDIUM",
                List.of()
        );

        when(questionRepository.findById(questionId)).thenReturn(testQuestion);
        when(questionMapper.toResponse(testQuestion)).thenReturn(expectedResponse);

        QuestionResponse response = questionService.getQuestionById(questionId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(questionId);
        verify(questionRepository).findById(questionId);
    }

    @Test
    @DisplayName("Should return null when question not found")
    void shouldReturnNullWhenQuestionNotFound() {
        when(questionRepository.findById(questionId)).thenReturn(null);

        QuestionResponse response = questionService.getQuestionById(questionId);

        assertThat(response).isNull();
        verify(questionRepository).findById(questionId);
    }

    @Test
    @DisplayName("Should count questions by exam ID")
    void shouldCountQuestionsByExamId() {
        when(questionRepository.countByExamId(examId)).thenReturn(25L);

        long count = questionService.countQuestionsByExamId(examId);

        assertThat(count).isEqualTo(25L);
        verify(questionRepository).countByExamId(examId);
    }
}

