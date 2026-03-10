package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.Namespace;

import java.util.List;
import java.util.Optional;

public interface NamespaceStore {
    void createNamespace(Namespace namespace);

    void updateNamespace(String namespaceId, Namespace namespace);

    Optional<Namespace> findNamespaceById(String namespaceId);

    List<Namespace> findAllNamespaces();

    void deleteNamespace(String namespaceId);

    boolean existsNamespaceById(String namespaceId);

    boolean existsNamespaceByName(String name);
}
