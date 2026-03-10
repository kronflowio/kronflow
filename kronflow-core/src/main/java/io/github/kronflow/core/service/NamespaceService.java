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

    public void createNamespace(String name, String description, String createdBy, String updatedBy) {
        if (store.existsByName(name)) {
            throw new DuplicateNameException("Namespace already exists!");
        }

        Namespace namespace = new Namespace(idGenerator.generateId(), name, description, createdBy, updatedBy);
        store.create(namespace);
    }

    public void updateNameSpace(String id, String name, String description, String updatedBy) {
        if (!store.existsById(id)) {
            throw new IllegalStateException("Namespace doesn't exist!");
        }

        if (store.existsByName(name)) {
            throw new DuplicateNameException("Namespace with the same name exists!");
        }

        Namespace namespace = store.findById(id).get();
        namespace.rename(name);
        namespace.updateDescription(description);
        namespace.modifyUpdatedBy(updatedBy);

        store.update(id, namespace);
    }

    public Optional<Namespace> getNamespaceById(String id) {
        return store.findById(id);
    }

    public List<Namespace> getAllNamespaces() {
        return store.findAll();
    }

    public void deleteNamespace(String namespaceId) {
        if (store.existsById(namespaceId)) {
            store.delete(namespaceId);
        }
    }
}
