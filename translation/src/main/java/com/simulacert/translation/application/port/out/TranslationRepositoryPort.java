package com.simulacert.translation.application.port.out;

import com.simulacert.translation.domain.Translation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TranslationRepositoryPort {
    Optional<Translation> find(String type, UUID entityId, String language);

    List<Translation> findAllByTypeAndEntityIdsAndLanguage(String type, Collection<UUID> entityIds, String language);

    Translation save(Translation translation);
}

