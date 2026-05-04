package com.simulacert.translation.application.service;

import com.simulacert.common.ClockPort;
import com.simulacert.infrastructure.xray.XRaySubsegment;
import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.translation.application.dto.TranslateFieldCommand;
import com.simulacert.translation.application.dto.TranslationResponse;
import com.simulacert.translation.application.port.in.TranslationUseCase;
import com.simulacert.translation.application.port.out.TranslationRepositoryPort;
import com.simulacert.translation.domain.Translation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService implements TranslationUseCase {

    private static final Double TEMPERATURE = 0.0;
    private static final Integer MAX_TOKENS = 600;

    private final TranslationRepositoryPort repository;
    private final ExplanationLLMPort llm;
    private final ClockPort clock;

    @Value("${app.llm.openai.model:gpt-4o-mini}")
    private String modelName;

    @Override
    @Transactional
    @XRaySubsegment("translation.translateField")
    public TranslationResponse translateField(TranslateFieldCommand command) {
        log.info("Translating {}:{} content={} to {}", command.entityType(), command.entityId(), command.content(), command.language());
        return generateAndPersist(command);
    }

    @Transactional
    @XRaySubsegment("translation.getOrTranslate")
    public String getOrTranslate(String type, UUID entityId, String text, String language) {
        Optional<String> string = repository.find(type, entityId, language).map(Translation::getValue);
        if (string.isPresent()) {
            return string.get();
        }

        TranslateFieldCommand command = new TranslateFieldCommand(type, entityId, text, language);
        return generateAndPersist(command).value();
    }

    @Transactional(readOnly = true)
    @XRaySubsegment("translation.findExisting")
    public Map<UUID, String> getExistingTranslations(String type, Map<UUID, String> fieldByEntityId, String language) {
        if (fieldByEntityId == null || fieldByEntityId.isEmpty()) {
            return Map.of();
        }

        var translations = repository.findAllByTypeAndEntityIdsAndLanguage(type, fieldByEntityId.keySet(), language);

        Map<UUID, String> result = new HashMap<>();
        for (var t : translations) {
            String field = fieldByEntityId.get(t.getEntityId());

            if (field != null && field.equals(t.getContent())) {
                result.put(t.getEntityId(), t.getValue());
            }
        }
        return result;
    }

    private TranslationResponse generateAndPersist(TranslateFieldCommand command) {
        LLMRequest llmRequest = new LLMRequest(
                PromptTemplates.systemPrompt(),
                PromptTemplates.userPrompt(command.content(), command.language()),
                TEMPERATURE,
                MAX_TOKENS,
                modelName
        );

        LLMResult result = llm.generate(llmRequest);
        String content = normalize(result.content());

        Translation translation = Translation.createLLM(
                command.entityType(),
                command.entityId(),
                command.content(),
                command.language(),
                content,
                clock.now()
        );

        repository.save(translation);
        log.info("Translation generated using {} - {}", result.provider(), result.modelName());

        return toResponse(translation);
    }

    private String normalize(String content) {
        if (content == null) return "";

        return content.strip();
    }

    private TranslationResponse toResponse(Translation t) {
        return new TranslationResponse(
                t.getId(),
                t.getEntityType(),
                t.getEntityId(),
                t.getContent(),
                t.getLanguage(),
                t.getValue(),
                t.getSource() != null ? t.getSource().name().toLowerCase() : null,
                t.isReviewed()
        );
    }
}

