package br.com.simulaaws.stats.infrastructure.persistence;

import br.com.simulaaws.stats.application.dto.AttemptHistoryItemDto;
import br.com.simulaaws.stats.application.dto.AwsDomainStatsDto;
import br.com.simulaaws.stats.application.dto.UserStatsDto;
import br.com.simulaaws.stats.application.port.out.StatsQueryPort;
import br.com.simulaaws.stats.infrastructure.persistence.mapper.AttemptHistoryMapper;
import br.com.simulaaws.stats.infrastructure.persistence.mapper.AwsDomainStatsMapper;
import br.com.simulaaws.stats.infrastructure.persistence.mapper.UserStatsMapper;
import br.com.simulaaws.stats.infrastructure.persistence.row.AttemptHistoryRow;
import br.com.simulaaws.stats.infrastructure.persistence.row.AwsDomainStatsRow;
import br.com.simulaaws.stats.infrastructure.persistence.row.UserStatsRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class StatsQueryAdapter implements StatsQueryPort {

    private final JdbcTemplate jdbcTemplate;
    private final UserStatsMapper mapper;
    private final AttemptHistoryMapper historyMapper;
    private final AwsDomainStatsMapper domainStatsMapper;

    @Override
    public UserStatsDto getUserStats(UUID userId) {
        UserStatsRow row = executeUserStatsQuery(userId);
        return mapper.toDto(row);
    }

    @Override
    public List<AttemptHistoryItemDto> getAttemptHistory(UUID userId) {
        List<AttemptHistoryRow> rows = executeAttemptHistoryQuery(userId);
        return historyMapper.toDtoList(rows);
    }

    @Override
    public List<AwsDomainStatsDto> getStatsByAwsDomain(UUID userId) {
        List<AwsDomainStatsRow> rows = executeAwsDomainStatsQuery(userId);
        return domainStatsMapper.toDtoList(rows);
    }

    private UserStatsRow executeUserStatsQuery(UUID userId) {
        String sql = """
                SELECT 
                    ? as user_id,
                    COUNT(a.id) as total_attempts,
                    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_attempts,
                    COALESCE(AVG(CASE WHEN a.status = 'COMPLETED' THEN a.score END), 0) as average_score,
                    MAX(a.score) as best_score,
                    MAX(a.started_at) as last_attempt_at
                FROM attempts a
                WHERE a.user_id = ?
                """;

        return jdbcTemplate.queryForObject(sql, this::mapUserStatsRow, userId, userId);
    }

    private List<AttemptHistoryRow> executeAttemptHistoryQuery(UUID userId) {
        String sql = """
                SELECT 
                    a.id,
                    a.exam_id,
                    e.title as exam_title,
                    a.started_at,
                    a.finished_at,
                    a.score,
                    a.status
                FROM attempts a
                LEFT JOIN exams e ON e.id = a.exam_id
                WHERE a.user_id = ?
                ORDER BY a.started_at DESC
                """;

        return jdbcTemplate.query(sql, this::mapAttemptHistoryRow, userId);
    }

    private List<AwsDomainStatsRow> executeAwsDomainStatsQuery(UUID userId) {
        String sql = """
                SELECT 
                    q.domain,
                    COUNT(DISTINCT aq.question_id) as total_questions,
                    0 as correct_answers,
                    0.0 as accuracy_rate
                FROM attempts a
                JOIN attempt_questions aq ON aq.attempt_id = a.id
                JOIN questions q ON q.id = aq.question_id
                WHERE a.user_id = ?
                GROUP BY q.domain
                ORDER BY q.domain
                """;

        return jdbcTemplate.query(sql, this::mapAwsDomainStatsRow, userId);
    }

    private UserStatsRow mapUserStatsRow(ResultSet rs, int rowNum) throws SQLException {
        return new UserStatsRow(
                UUID.fromString(rs.getString("user_id")),
                rs.getInt("total_attempts"),
                rs.getInt("completed_attempts"),
                rs.getDouble("average_score"),
                rs.getObject("best_score") != null ? rs.getInt("best_score") : null,
                rs.getTimestamp("last_attempt_at") != null
                        ? rs.getTimestamp("last_attempt_at").toInstant()
                        : null
        );
    }

    private AttemptHistoryRow mapAttemptHistoryRow(ResultSet rs, int rowNum) throws SQLException {
        return new AttemptHistoryRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("exam_id")),
                rs.getString("exam_title"),
                rs.getTimestamp("started_at").toInstant(),
                rs.getTimestamp("finished_at") != null
                        ? rs.getTimestamp("finished_at").toInstant()
                        : null,
                rs.getObject("score") != null ? rs.getInt("score") : null,
                rs.getString("status")
        );
    }

    private AwsDomainStatsRow mapAwsDomainStatsRow(ResultSet rs, int rowNum) throws SQLException {
        return new AwsDomainStatsRow(
                rs.getString("domain"),
                rs.getInt("total_questions"),
                rs.getInt("correct_answers"),
                rs.getDouble("accuracy_rate")
        );
    }
}
