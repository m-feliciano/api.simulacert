package br.com.simulaaws.clients.exam;

import br.com.simulaaws.clients.config.FeignConfig;
import br.com.simulaaws.clients.exam.dto.QuestionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "question-client",
        contextId = "questionClient", // Unique context ID for this Feign client
        url = "${clients.exam.url}",
        configuration = {FeignConfig.class}
)
public interface QuestionClient {

    @GetMapping(path = "/api/v1/questions/exam/{examId}")
    List<QuestionResponse> findByExamId(@PathVariable("examId") UUID examId);

    @GetMapping(path = "/api/v1/questions/{questionId}")
    QuestionResponse findById(@PathVariable("questionId") UUID questionId);
}

