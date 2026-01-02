# Módulo Stats - Regras de Arquitetura

## ⚠️ REGRAS OBRIGATÓRIAS

### 1. Read-Only Estrito

- Stats **APENAS LÊ** dados já persistidos
- Stats **NÃO RECALCULA** score ou qualquer métrica de domínio
- Stats **NÃO USA** entidades de domínio (Attempt, Answer, etc)

### 2. Apenas SQL/Projections

- Todas as consultas são SQL direto
- Usa records de projection (DTOs de leitura)
- Agregações feitas no banco de dados

### 3. Sem Dependência de Domínio

- Stats não importa `br.com.simulaaws.attempt.domain`
- Stats não importa `br.com.simulaaws.exam.domain`
- Stats usa apenas `common` e suas próprias projections

### 4. Implementação no Módulo App

- `StatsQueryPort` é implementado no módulo `app`
- Usa `@Query` nativo do Spring Data JPA
- Retorna DTOs/Records, não entidades

## Exemplo de Implementação (app module)

```java

@Repository
public interface StatsJpaQueryAdapter extends StatsQueryPort {

    @Query(value = """
            SELECT 
                u.id as userId,
                COUNT(a.id) as totalAttempts,
                COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completedAttempts,
                AVG(a.score) as averageScore,
                MAX(a.score) as bestScore,
                MAX(a.started_at) as lastAttemptAt
            FROM users u
            LEFT JOIN attempts a ON a.user_id = u.id
            WHERE u.id = :userId
            GROUP BY u.id
            """, nativeQuery = true)
    UserStatsProjection getUserStats(UUID userId);
}
```

## Por que essas regras?

1. **Evita acoplamento**: Stats não depende de regras de negócio
2. **Performance**: Agregações no banco são mais rápidas
3. **Simplicidade**: Leitura direta, sem lógica complexa
4. **Manutenibilidade**: Mudanças no domínio não quebram stats

## O que NÃO fazer

❌ `attempt.calculateScore()` dentro de Stats
❌ Importar `Attempt` de outro módulo
❌ Recalcular métricas em Java
❌ Usar repositories de domínio

## O que fazer

✅ SQL nativo com agregações
✅ Records de projection
✅ Leitura direta de tabelas
✅ Cache de consultas (se necessário)

