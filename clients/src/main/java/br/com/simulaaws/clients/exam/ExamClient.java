package br.com.simulaaws.clients.exam;

import br.com.simulaaws.clients.config.FeignConfig;
import br.com.simulaaws.clients.exam.dto.ExamResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "exam-client",
        contextId = "examClient",
        url = "${clients.exam.url}",
        configuration = {FeignConfig.class}
)
public interface ExamClient {

    @GetMapping(path = "/api/v1/exams/{examId}")
    ExamResponse getExamById(@PathVariable("examId") UUID examId);

    @GetMapping(path = "/api/v1/exams/{examId}/exists")
    boolean existsById(@PathVariable("examId") UUID examId);
}

