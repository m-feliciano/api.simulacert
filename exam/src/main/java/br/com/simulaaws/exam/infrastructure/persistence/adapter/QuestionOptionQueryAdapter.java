package br.com.simulaaws.exam.infrastructure.persistence.adapter;

import br.com.simulaaws.exam.application.port.out.QuestionOptionQueryPort;
import br.com.simulaaws.exam.infrastructure.persistence.repository.QuestionOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class QuestionOptionQueryAdapter implements QuestionOptionQueryPort {

    private final QuestionOptionRepository repository;

    @Override
    public List<QuestionOptionDto> findByQuestionId(UUID questionId) {
        return repository.findByQuestionId(questionId).stream()
                .map(qo -> new QuestionOptionDto(qo.getOptionKey(), qo.getOptionText(), qo.getIsCorrect()))
                .toList();
    }
}