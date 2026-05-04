package com.simulacert.translation.application.service;

import com.simulacert.common.ClockPort;
import com.simulacert.llm.application.dto.LLMResult;
import com.simulacert.llm.application.port.out.ExplanationLLMPort;
import com.simulacert.translation.application.port.out.TranslationRepositoryPort;
import com.simulacert.translation.domain.Translation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    TranslationRepositoryPort repository;

    @Mock
    ExplanationLLMPort llm;

    @Mock
    ClockPort clock;

    @InjectMocks
    TranslationService service;

    @Test
    void shouldReturnExistingTranslationWithoutCallingLLM() {
        UUID entityId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-04T00:00:00Z");

        Translation existing = Translation.createLLM("question", entityId, "text", "en", "Hello", now);

        when(repository.find("question", entityId, "en")).thenReturn(Optional.of(existing));

        String value = service.getOrTranslate("question", entityId, "text", "en");

        assertThat(value).isEqualTo("Hello");
        verify(llm, never()).generate(any());
    }

    @Test
    void shouldCallLLMAndPersistWhenMissing() {
        UUID entityId = UUID.randomUUID();
        Instant now = Instant.parse("2026-05-04T00:00:00Z");

        when(repository.find("question", entityId, "en")).thenReturn(Optional.empty());
        when(llm.generate(any())).thenReturn(new LLMResult("Hello world", "gpt-4.1-mini", "openai"));
        when(clock.now()).thenReturn(now);

        // ensure @Value content is set (otherwise can be null under unit tests)
        ReflectionTestUtils.setField(service, "modelName", "gpt-4o-mini");

        ArgumentCaptor<Translation> captor = ArgumentCaptor.forClass(Translation.class);
        when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));


        String value = service.getOrTranslate("question", entityId, "text", "en");

        assertThat(value).isEqualTo("Hello world");
        verify(llm).generate(any());
        assertThat(captor.getValue().getEntityType()).isEqualTo("question");
        assertThat(captor.getValue().getLanguage()).isEqualTo("en");
    }
}

