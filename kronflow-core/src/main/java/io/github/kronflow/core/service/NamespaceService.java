package io.github.kronflow.core.service;

import io.github.kronflow.core.exception.DuplicateNameException;
import io.github.kronflow.core.model.Namespace;
import io.github.kronflow.core.spi.IdGenerator;
import io.github.kronflow.core.spi.NamespaceStore;

import java.util.List;
import java.util.Optional;

public class NamespaceService {
    private final NamespaceStore store;
    private final IdGenerator idGenerator;

    public NamespaceService(NamespaceStore store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    public Namespace createNamespace(String name, String description, String createdBy, String updatedBy) {
        if (store.existsNamespaceByName(name)) {
            throw new DuplicateNameException("Namespace already exists: " + name);
        }

        Namespace namespace = new Namespace(idGenerator.generateId(), name, description, createdBy, updatedBy);
        store.createNamespace(namespace);
        return namespace;
    }

    public Namespace updateNameSpace(String namespaceId, String name, String description, String updatedBy) {
        Namespace namespace = store.findNamespaceById(namespaceId)
                .orElseThrow(() -> new IllegalStateException("Namespace doesn't exist: " + namespaceId));

        if (!namespace.getName().equals(name) && store.existsNamespaceByName(name)) {
            throw new DuplicateNameException("Namespace already exists: " + namespaceId);
        }

        namespace.rename(name);
        namespace.updateDescription(description);
        namespace.modifyUpdatedBy(updatedBy);

        store.updateNamespace(namespaceId, namespace);
        return namespace;
    }

    public Optional<Namespace> getNamespaceById(String id) {
        return store.findNamespaceById(id);
    }

    public List<Namespace> getAllNamespaces() {
        return store.findAllNamespaces();
    }

    public void deleteNamespace(String namespaceId) {
        if (!store.existsNamespaceById(namespaceId)) {
            throw new IllegalStateException("Namespace doesn't exist: " + namespaceId);
        }
        store.deleteNamespace(namespaceId);
    }
}
