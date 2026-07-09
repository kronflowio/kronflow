package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.Namespace;

import java.util.List;
import java.util.Optional;

public interface NamespaceStore {
    /**
     * Persists a new namespace. Throws DuplicateNameException if name exists.
     */
    void create(Namespace namespace);

    void update(Namespace namespace);

    Optional<Namespace> findById(String namespaceId);

    Optional<Namespace> findByName(String name);

    boolean existsById(String namespaceId);

    boolean existsByName(String name);

    List<Namespace> findAll();

    void deleteById(String namespaceId);
}