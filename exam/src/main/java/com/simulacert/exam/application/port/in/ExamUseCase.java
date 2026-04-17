package com.simulacert.exam.application.port.in;

import com.simulacert.exam.application.dto.request.CreateExamRequest;
import com.simulacert.exam.application.dto.request.UpdateExamRequest;
import com.simulacert.exam.application.dto.response.ExamResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ExamUseCase {

    ExamResponse getExamById(UUID examId);

    boolean examExists(UUID examId);

    List<ExamResponse> getAllExams();

    ExamResponse createExam(CreateExamRequest request);

    ExamResponse updateExam(UUID examId, UpdateExamRequest request);

    void deleteExam(UUID examId);

    void importExamsFiles(List<MultipartFile> files);

    ExamResponse getExamBySlug(String slug);

    void importExamsFilesFromDirectory();
}
