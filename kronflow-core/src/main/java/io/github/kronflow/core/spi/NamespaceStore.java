package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.Namespace;

import java.util.List;
import java.util.Optional;

public interface NamespaceStore {
    void create(Namespace namespace);

    void update(String namespaceId, Namespace namespace);

    Optional<Namespace> findById(String namespaceId);

    boolean existsById(String namespaceId);

    List<Namespace> findAll();

    void deleteById(String namespaceId);

    boolean existsNamespaceByName(String name);
}
