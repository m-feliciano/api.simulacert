package com.simulacert.translation.infrastructure.persistence;

import com.simulacert.translation.application.port.out.TranslationRepositoryPort;
import com.simulacert.translation.domain.Translation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TranslationRepositoryAdapter implements TranslationRepositoryPort {

    private final JpaTranslationRepository jpaRepository;

    @Override
    public Optional<Translation> find(String type, UUID entityId, String language) {
        return jpaRepository.findByEntityTypeAndEntityIdAndLanguage(type, entityId, language);
    }

    @Override
    public List<Translation> findAllByTypeAndEntityIdsAndLanguage(String type, Collection<UUID> entityIds, String language) {
        return jpaRepository.findByEntityTypeAndEntityIdInAndLanguage(type, entityIds, language);
    }

    @Override
    public Translation save(Translation translation) {
        return jpaRepository.save(translation);
    }
}

