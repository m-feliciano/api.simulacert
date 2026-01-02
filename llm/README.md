# Módulo LLM - Guardrails e Limites Operacionais

## ⚠️ REGRAS OBRIGATÓRIAS - GUARDRAILS

### 1. Timeouts Estritos
- **Timeout máximo**: 2-3 segundos por chamada
- **Health check**: 1 segundo
- **Nunca bloqueia fluxo crítico**

### 2. Sem Retry Automático
- ❌ NÃO implementar retry automático
- ❌ NÃO usar bibliotecas de retry (Resilience4j, etc)
- ✅ Uma tentativa apenas
- ✅ Falha rápida e silenciosa

### 3. Fallback Silencioso
- Se timeout/erro → `Optional.empty()` ou texto padrão
- **Nunca lança exception** para o chamador
- **Nunca falha a operação principal**

### 4. Fora do Fluxo Crítico
- LLM **NÃO decide** resultado de prova
- LLM **NÃO calcula** score
- LLM **NÃO valida** respostas
- LLM **APENAS** fornece assistência educacional

## Configuração Esperada (application.yml)

```yaml
llm:
  enabled: true
  timeout: 3000  # 3 segundos
  provider: claude
  api-key: ${CLAUDE_API_KEY}
  model: claude-3-5-sonnet-20241022
  fallback:
    explanation: "Revise os conceitos relacionados a esta questão no AWS documentation."
    study-tips: "Foque nos domínios onde você teve mais dificuldade."
```

## Exemplo de Implementação (app module)

```java
@Service
public class ClaudeAssistantAdapter implements LearningAssistantPort {
    
    private static final Duration TIMEOUT = Duration.ofSeconds(3);
    private static final String FALLBACK_EXPLANATION = "Revise os conceitos...";
    
    @Override
    public Optional<String> explainIncorrectAnswer(...) {
        try {
            return CompletableFuture
                .supplyAsync(() -> callClaude(...))
                .orTimeout(3, TimeUnit.SECONDS)
                .exceptionally(ex -> null)
                .get();
        } catch (Exception e) {
            log.debug("LLM call failed, returning empty", e);
            return Optional.empty();
        }
    }
}
```

## Monitoramento

### Métricas Obrigatórias
- Taxa de sucesso/falha
- Tempo médio de resposta
- Timeout rate
- Disponibilidade do serviço

### Logs
- Debug level para falhas (não error)
- Não logar API keys
- Não logar conteúdo completo das respostas

## Casos de Uso

### ✅ Permitido
- Explicar questões após tentativa completa
- Gerar dicas de estudo baseadas em histórico
- Sugerir tópicos para revisar

### ❌ Proibido
- Validar respostas durante prova
- Calcular score ou métricas
- Bloquear fluxo de finalização de tentativa
- Dar respostas corretas durante prova ativa

## Testes

### Testes de Timeout
```java
@Test
void shouldTimeoutAfter3Seconds() {
    // Mock que demora 5 segundos
    var result = llmPort.explainIncorrectAnswer(...);
    
    assertThat(result).isEmpty();
    // Deve retornar em ~3s, não 5s
}
```

### Testes de Fallback
```java
@Test
void shouldReturnFallbackOnError() {
    // Mock que lança exception
    var result = llmPort.generateStudyTips(...);
    
    assertThat(result).isEqualTo(FALLBACK_TEXT);
    assertThat(result).isNotEmpty();
}
```

## Dependências Externas

```groovy
// app/build.gradle
dependencies {
    // Cliente HTTP com timeout configurável
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    
    // OU
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
}
```

## Circuit Breaker (Opcional, mas recomendado)

```java
@CircuitBreaker(
    name = "llm",
    fallbackMethod = "fallbackExplanation"
)
public Optional<String> explainIncorrectAnswer(...) {
    // ... implementação
}

private Optional<String> fallbackExplanation(...) {
    return Optional.empty();
}
```

## Resumo

| Característica | Valor |
|----------------|-------|
| Timeout | 3 segundos |
| Retry | Nenhum |
| Fallback | Silencioso |
| Criticidade | Baixa (não-essencial) |
| Exception | Nunca propaga |
| Logging | Debug level |
| Circuit Breaker | Recomendado |

**LLM é apoio educacional. Sistema funciona perfeitamente sem ele.**

