package com.simulacert.exam.application.service;

import com.simulacert.exam.application.dto.request.CreateExamRequest;
import com.simulacert.exam.application.dto.request.ExamImportDto;
import com.simulacert.exam.application.dto.request.OptionImportDto;
import com.simulacert.exam.application.dto.request.QuestionImportDto;
import com.simulacert.exam.application.dto.request.UpdateExamRequest;
import com.simulacert.exam.application.dto.response.ExamImportResponse;
import com.simulacert.exam.application.dto.response.ExamResponse;
import com.simulacert.exam.application.mapper.ExamMapper;
import com.simulacert.exam.application.port.out.ExamRepositoryPort;
import com.simulacert.exam.application.port.out.QuestionRepositoryPort;
import com.simulacert.exam.domain.Exam;
import com.simulacert.exam.domain.Question;
import com.simulacert.exam.infrastructure.persistence.repository.QuestionOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamService Tests")
class ExamServiceTest {

    @Mock
    private ExamRepositoryPort examRepository;

    @Mock
    private QuestionRepositoryPort questionRepository;

    @Mock
    private QuestionOptionRepository questionOptionRepository;

    @Mock
    private ExamMapper examMapper;

    @InjectMocks
    private ExamService examService;

    private Exam testExam;
    private UUID examId;

    @BeforeEach
    void setUp() {
        testExam = Exam.create("Test Exam", "Test Description", "test-slug");
        examId = testExam.getId();
    }

    @Test
    @DisplayName("Should create exam successfully")
    void shouldCreateExamSuccessfully() {
        CreateExamRequest request = new CreateExamRequest("New Exam", "Description", "Slug");
        ExamResponse expectedResponse = new ExamResponse(examId, "New Exam", "Description");

        when(examRepository.save(any(Exam.class))).thenReturn(testExam);
        when(examMapper.toResponse(testExam)).thenReturn(expectedResponse);

        ExamResponse response = examService.createExam(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("New Exam");
        verify(examRepository).save(any(Exam.class));
        verify(examMapper).toResponse(testExam);
    }

    @Test
    @DisplayName("Should get exam by ID successfully")
    void shouldGetExamByIdSuccessfully() {
        ExamResponse expectedResponse = new ExamResponse(examId, "Test Exam", "Test Description");

        when(examRepository.findById(examId)).thenReturn(Optional.of(testExam));
        when(examMapper.toResponse(any(Exam.class)))
                .thenReturn(expectedResponse);

        ExamResponse response = examService.getExamById(examId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(examId);
        verify(examRepository).findById(examId);
    }

    @Test
    @DisplayName("Should return null when exam not found")
    void shouldReturnNullWhenExamNotFound() {
        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        ExamResponse response = examService.getExamById(examId);

        assertThat(response).isNull();
        verify(examRepository).findById(examId);
    }

    @Test
    @DisplayName("Should get all exams")
    void shouldGetAllExams() {
        Exam exam2 = Exam.create("Exam 2", "Description 2", "exam-2-slug");

        when(examRepository.findAll()).thenReturn(List.of(testExam, exam2));
        when(examMapper.toResponse(any(Exam.class)))
                .thenReturn(new ExamResponse(testExam.getId(), testExam.getTitle(), testExam.getDescription()))
                .thenReturn(new ExamResponse(exam2.getId(), exam2.getTitle(), exam2.getDescription()));

        List<ExamResponse> responses = examService.getAllExams();

        assertThat(responses).hasSize(2);
        verify(examRepository).findAll();
        verify(examMapper, times(2)).toResponse(any(Exam.class));
    }

    @Test
    @DisplayName("Should update exam successfully")
    void shouldUpdateExamSuccessfully() {
        UpdateExamRequest request = new UpdateExamRequest("Updated Title", "Updated Description");
        ExamResponse expectedResponse = new ExamResponse(examId, "Updated Title", "Updated Description");

        when(examRepository.findById(examId)).thenReturn(Optional.of(testExam));
        when(examRepository.save(testExam)).thenReturn(testExam);
        when(examMapper.toResponse(testExam)).thenReturn(expectedResponse);

        ExamResponse response = examService.updateExam(examId, request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Updated Title");
        verify(examRepository).findById(examId);
        verify(examRepository).save(testExam);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent exam")
    void shouldThrowExceptionWhenUpdatingNonExistentExam() {
        UpdateExamRequest request = new UpdateExamRequest("Updated Title", "Updated Description");

        when(examRepository.findById(examId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> examService.updateExam(examId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exam not found: " + examId);

        verify(examRepository).findById(examId);
        verify(examRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete exam successfully")
    void shouldDeleteExamSuccessfully() {
        when(examRepository.existsById(examId)).thenReturn(true);

        examService.deleteExam(examId);

        verify(examRepository).existsById(examId);
        verify(examRepository).deleteById(examId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent exam")
    void shouldThrowExceptionWhenDeletingNonExistentExam() {
        when(examRepository.existsById(examId)).thenReturn(false);

        assertThatThrownBy(() -> examService.deleteExam(examId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Exam not found: " + examId);

        verify(examRepository).existsById(examId);
        verify(examRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should check if exam exists")
    void shouldCheckIfExamExists() {
        when(examRepository.existsById(examId)).thenReturn(true);

        boolean exists = examService.examExists(examId);

        assertThat(exists).isTrue();
        verify(examRepository).existsById(examId);
    }

    @Test
    @DisplayName("Should check if exam has minimum questions")
    void shouldCheckIfExamHasMinimumQuestions() {
        when(questionRepository.countByExamId(examId)).thenReturn(15L);

        boolean hasMinimum = examService.hasMinimumQuestions(examId);

        assertThat(hasMinimum).isTrue();
        verify(questionRepository).countByExamId(examId);
    }

    @Test
    @DisplayName("Should return false when exam has less than minimum questions")
    void shouldReturnFalseWhenExamHasLessThanMinimumQuestions() {
        when(questionRepository.countByExamId(examId)).thenReturn(5L);

        boolean hasMinimum = examService.hasMinimumQuestions(examId);

        assertThat(hasMinimum).isFalse();
        verify(questionRepository).countByExamId(examId);
    }

    @Test
    @DisplayName("Should import exam successfully")
    void shouldImportExamSuccessfully() {
        List<OptionImportDto> options = List.of(
                new OptionImportDto("A", "Option A", false),
                new OptionImportDto("B", "Option B", true)
        );

        List<QuestionImportDto> questions = List.of(
                new QuestionImportDto("Question 1?", "EASY", "AWS", options),
                new QuestionImportDto("Question 2?", "MEDIUM", "AWS", options)
        );

        ExamImportDto importDto = new ExamImportDto("Imported Exam", "Description", "imported-exam-slug", questions);

        Question mockQuestion = Question.create(examId, "Question 1?", "AWS", "EASY");

        when(examRepository.save(any(Exam.class))).thenReturn(testExam);
        when(questionRepository.save(any(Question.class))).thenReturn(mockQuestion);
        when(questionOptionRepository.saveAll(anyList())).thenReturn(List.of());

        ExamImportResponse response = examService.importExam(importDto);

        assertThat(response).isNotNull();
        assertThat(response.examId()).isEqualTo(examId);
        assertThat(response.title()).isEqualTo("Test Exam");
        assertThat(response.questionsImported()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("SUCCESS");
        verify(examRepository).save(any(Exam.class));
        verify(questionRepository, times(2)).save(any(Question.class));
        verify(questionOptionRepository, times(2)).saveAll(anyList());
    }
}

