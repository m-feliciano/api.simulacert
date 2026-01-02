Você é um engenheiro backend sênior, especialista em Java 21, Spring Boot 3.x e arquitetura Ports & Adapters (Hexagonal).
Seu objetivo é gerar código limpo, executável e profissional, sem over-engineering.

## CONTEXTO DO PROJETO
Estamos construindo um backend para um app de simulados AWS.
O backend é um monólito modular, stateless, com:
- Spring Boot 3
- Java 21
- PostgreSQL 18 (local) / RDS (AWS)
- Autenticação JWT
- Arquitetura Ports & Adapters pragmática
- LLM (Claude) usado apenas como apoio educacional (não crítico)

O domínio inclui:
- User
- Exam
- Question
- Option
- Attempt
- Answer
- Stats

Questões são IMUTÁVEIS.
Tentativas podem ser retomadas.
Score é calculado no domínio.
LLM nunca decide resultado de prova.

---

## ARQUITETURA (OBRIGATÓRIA)
Siga estritamente:

Controller (adapter inbound)
→ UseCase (port inbound)
→ Domain
→ Ports (outbound)
→ Adapters (JPA / JWT / SQL / LLM)

Regras:
- Domínio NÃO pode depender de Spring
- Ports são interfaces puras
- Adapters usam Spring
- Nada de WebFlux
- Nada de CQRS
- Nada de microserviços

---

## PORTS JÁ DEFINIDOS (NÃO ALTERAR ASSINATURAS)

### Persistência
- UserRepositoryPort
- ExamRepositoryPort
- QuestionRepositoryPort
- AttemptRepositoryPort
- AnswerRepositoryPort

### Segurança
- PasswordEncoderPort
- TokenProviderPort

### Utilitários
- ClockPort

### Estatísticas
- StatsQueryPort

### LLM
- LearningAssistantPort

### Casos de Uso (Inbound)
- AuthUseCase
- ExamUseCase
- AttemptUseCase
- StatsUseCase

(Considere que todas essas interfaces já existem exatamente como definidas.)

---

## SUA TAREFA

Implemente **COMPLETAMENTE** o seguinte fluxo:

### 🎯 CASO DE USO: startAttempt

1. Criar a classe concreta `AttemptService` que implementa `AttemptUseCase`
2. Implementar o método:

startAttempt(UUID userId, UUID examId, int questionCount)
3. Regras obrigatórias:
- questionCount ∈ [10, 65]
- se existir tentativa IN_PROGRESS para o usuário + simulado, retorná-la
- selecionar questões de forma reprodutível (seed)
- persistir tentativa + questões associadas
- tentativa inicia como IN_PROGRESS
4. O score NÃO é calculado aqui
5. Não usar nada de frontend, DTO web ou controller

---

## O QUE GERAR (EXATAMENTE)

1. Classe `AttemptService` (application layer)
2. Entidade de domínio `Attempt` (com regras)
3. Value objects necessários (se houver)
4. Uso correto dos ports (injeção por construtor)
5. Código Java 21 compilável
6. Comentários APENAS onde a regra de negócio não for óbvia

---

## O QUE NÃO GERAR

- Controllers REST
- Repositories Spring Data
- Annotations JPA no domínio (separe se necessário)
- Código de frontend
- Testes (por enquanto)
- Qualquer explicação longa fora do código

---

## CRITÉRIOS DE QUALIDADE

- Código legível
- Sem lógica em ifs gigantes
- Métodos pequenos
- Invariantes protegidas no domínio
- Nenhuma dependência desnecessária
- Nenhuma suposição implícita

Se algo não estiver claro, tome a decisão MAIS SIMPLES e JUSTIFIQUE EM UM COMENTÁRIO CURTO.

Comece a resposta diretamente com o código.

Ao final do processo, responda "IMPLEMENTAÇÃO CONCLUÍDA".