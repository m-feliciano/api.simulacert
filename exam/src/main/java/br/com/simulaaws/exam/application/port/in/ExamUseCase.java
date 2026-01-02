package br.com.simulaaws.exam.application.port.in;

import br.com.simulaaws.exam.application.dto.CreateExamRequest;
import br.com.simulaaws.exam.application.dto.ExamResponse;
import br.com.simulaaws.exam.application.dto.UpdateExamRequest;

import java.util.List;
import java.util.UUID;

public interface ExamUseCase {

    ExamResponse getExamById(UUID examId);

    boolean examExists(UUID examId);

    List<ExamResponse> getAllExams();

    ExamResponse createExam(CreateExamRequest request);

    ExamResponse updateExam(UUID examId, UpdateExamRequest request);

    void deleteExam(UUID examId);
}
