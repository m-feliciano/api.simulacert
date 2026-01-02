package br.com.simulaaws.llm.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case para assistência de aprendizado via LLM.
 *
 * IMPORTANTE: LLM é apoio educacional, não é crítico.
 * Todas as operações são assíncronas/não-bloqueantes.
 */
public interface LearningAssistanceUseCase {

    /**
     * Solicita explicação para resposta incorreta.
     * Retorna imediatamente, explicação pode ser gerada de forma assíncrona.
     */
    Optional<String> requestExplanation(
        UUID questionId,
        UUID userAnswerId,
        UUID correctAnswerId
    );

    /**
     * Gera dicas de estudo baseadas em desempenho.
     * Sempre retorna algum texto (padrão ou gerado por LLM).
     */
    String getStudyRecommendations(UUID userId, List<String> weakDomains);
}

