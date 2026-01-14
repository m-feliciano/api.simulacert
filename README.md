# SimulaCert — API de Simulados AWS (Monólito Modular)

Este repositório contém a API do SimulaCert: um sistema de simulados / exames focado em conteúdos AWS. O projeto é organizado como um monólito modular com vários módulos separados por responsabilidade (auth, exam, attempt, stats, llm, review, common, app).

Resumo rápido
- Linguagem: Java 21 (toolchain configurada)
- Build: Gradle (wrapper disponível)
- Framework: Spring Boot 3.5.x
- Persistência: PostgreSQL (runtime) + H2 (dev)
- API docs: OpenAPI (springdoc)

Índice
- Visão Geral
- Estrutura do Repositório
- Requisitos
- Como construir
- Como rodar (local / Docker)
- Configuração importante (variáveis de ambiente / application.yml)
- Módulos e responsabilidades
- Endpoints úteis
- Testes
- Contribuição
- Licença e Contato

Visão Geral

O backend é um "monólito modular": cada funcionalidade agrupa código em módulos Gradle independentes, mas a aplicação é executada como um único artefato a partir do módulo `app`.

Estrutura do Repositório (principais pastas)
- `app/` — Módulo de inicialização Spring Boot e ponto de entrada (`SimulaCertApplication`). Contém dependências e integrações com outros módulos.
- `auth/` — Autenticação e autorização (JWT).
- `common/` — Tipos, utilitários e contratos compartilhados.
- `exam/` — Modelos e lógica de provas/exames.
- `attempt/` — Execução e persistência de tentativas de prova.
- `stats/` — Cálculos e relatórios estatísticos.
- `llm/` — Integração com LLMs (documentação de guardrails em `llm/README.md`).
- `review/` — Módulo de revisão pós-tentativa.

Requisitos
- JDK 21 (Gradle toolchain configurada, mas recomendado ter JDK local)
- Gradle (wrapper incluso: use `./gradlew` ou `gradlew.bat` no Windows)
- Docker & Docker Compose (opcional para executar banco e dependências)

Como construir

No Windows PowerShell, a partir do diretório raiz do projeto:

```powershell
# Build do projeto (compila todos os módulos)
./gradlew clean build -x test

# Para rodar os testes também
./gradlew clean build
```

Como rodar localmente (modo desenvolvimento)

1. Configurar variáveis de ambiente necessárias (ver seção abaixo).
2. Rodar banco (Postgres) — você pode usar Docker Compose ou um Postgres local.
3. Iniciar a aplicação a partir do módulo `app`:

```powershell
# Roda a aplicação com o wrapper Gradle
./gradlew :app:bootRun
```

Como rodar com Docker Compose

Este repositório tem arquivos Docker Compose (por exemplo `docker-compose-dev.yaml`) para facilitar o ambiente de desenvolvimento. Exemplo:

```powershell
# Inicia dependências (Postgres, etc.) conforme definido no compose
docker compose -f docker-compose-dev.yaml up -d

# Em seguida, rode a aplicação localmente (ou crie uma imagem/container conforme sua necessidade)
./gradlew :app:bootRun
```

Configuração importante

As configurações principais ficam em `app/src/main/resources/application.yml`. Principais pontos:
- porta do servidor: `server.port` (padrão 8080)
- spring.profiles.active — ambiente ativo (p.ex. `dev`, `prod`)
- Flyway para migrações: `spring.flyway.*` (migrations em classpath:db/migration)
- Spring Data Web Pageable defaults configurados
- Springdoc OpenAPI path: `/v3/api-docs`
- Actuator: health e info expostos por padrão
- Logging: arquivo `logs/simulacert.log`

Módulos e responsabilidades (detalhado)
- app — ponto de entrada, configuração de web, segurança, dependências de infraestrutura (Flyway, Actuator, OpenAPI).
- auth — JWT e fluxos de autenticação; depende de `common`.
- common — entidades, DTOs, utilitários compartilhados entre módulos.
- exam — domínio de exame; validações e regras relacionadas à prova.
- attempt — fluxos de tentativa/execução de provas e persistência.
- stats — geração de métricas e relatórios.
- llm — adaptadores e configurações para chamadas a LLM (ver `llm/README.md` para guardrails e exemplos).
- review — funcionalidades relacionadas à revisão de questões após tentativas.

Endpoints úteis
- OpenAPI JSON: GET /v3/api-docs
- Swagger UI (quando habilitado): /swagger-ui
- Actuator health: /actuator/health
- Actuator info: /actuator/info

Licença e contato

Proprietary - © 2026 SimulaCert

Website: https://simulacert.com
API: https://api.simulacert.com

Última atualização: 2026-01-11
