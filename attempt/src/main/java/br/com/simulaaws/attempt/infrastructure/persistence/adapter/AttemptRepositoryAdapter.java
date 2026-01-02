package br.com.simulaaws.attempt.infrastructure.persistence.adapter;

import br.com.simulaaws.attempt.application.port.out.AttemptRepositoryPort;
import br.com.simulaaws.attempt.domain.Attempt;
import br.com.simulaaws.attempt.domain.AttemptStatus;
import br.com.simulaaws.attempt.infrastructure.persistence.repository.AttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AttemptRepositoryAdapter implements AttemptRepositoryPort {

    private final AttemptRepository repository;

    @Override
    public Attempt save(Attempt attempt) {
        return repository.save(attempt);
    }

    @Override
    public Optional<Attempt> findByUserIdAndExamIdAndStatus(UUID userId, UUID examId, AttemptStatus status) {
        return repository.findByUserIdAndExamIdAndStatus(userId, examId, status);
    }

    @Override
    public Optional<Attempt> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Attempt> findByUserIdOrderByStartedAtDesc(UUID userId) {
        return repository.findByUserIdOrderByStartedAtDesc(userId);
    }
}

