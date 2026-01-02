package br.com.simulaaws.exam.application.dto.response;

public record QuestionOptionResponse(
        String optionKey,
        String optionText,
        Boolean isCorrect
) {
}
