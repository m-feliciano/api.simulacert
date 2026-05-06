package com.simulacert.translation.infrastructure.persistence;

import com.simulacert.translation.domain.Translation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaTranslationRepository extends JpaRepository<Translation, UUID> {
    Optional<Translation> findByEntityTypeAndEntityIdAndLanguage(
            String entityType,
            UUID entityId,
            String language
    );

    List<Translation> findByEntityTypeAndEntityIdInAndLanguage(
            String entityType,
            Collection<UUID> entityIds,
            String language
    );
}

