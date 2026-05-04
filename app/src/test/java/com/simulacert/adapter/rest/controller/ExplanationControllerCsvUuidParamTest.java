package com.simulacert.adapter.rest.controller;

import com.simulacert.adapter.rest.controller.param.CsvUuidParamArgumentResolver;
import com.simulacert.exam.application.port.in.QuestionExplanationUseCase;
import com.simulacert.infrastructure.ratelimit.RateLimitFilter;
import com.simulacert.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ExplanationController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class)
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(ExplanationControllerCsvUuidParamTest.MvcTestConfig.class)
@ActiveProfiles("test")
class ExplanationControllerCsvUuidParamTest {

    @TestConfiguration
    static class MvcTestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new CsvUuidParamArgumentResolver());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionExplanationUseCase explanationUseCase;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Test
    void shouldParseCsvUuids() throws Exception {
        when(explanationUseCase.getExplanationsForQuestions(anyList(), any()))
                .thenReturn(List.of());

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/questions/explanations")
                        .param("questionIds", id1 + "," + id2))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400OnInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/v1/questions/explanations")
                        .param("questionIds", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}


