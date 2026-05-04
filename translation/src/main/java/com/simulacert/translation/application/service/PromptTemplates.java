package com.simulacert.translation.application.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class PromptTemplates {

    static String systemPrompt() {
        return "You are a professional translator specialized in AWS/Azure/GCP certification exams.";
    }

    static String userPrompt(String sourceText, String targetLanguage) {
        return """
                Translate the following text from Portuguese to %s.
                
                Rules:
                - Keep technical terms consistent with official terminologies
                - Do NOT explain anything
                - Do NOT add extra text
                - Preserve original meaning exactly
                - Keep answer concise
                
                Text:
                %s
                """.formatted(targetLanguage, sourceText);
    }
}

