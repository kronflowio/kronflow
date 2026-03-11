package io.github.kronflow.core.service;

import io.github.kronflow.core.exception.DuplicateNameException;
import io.github.kronflow.core.model.Namespace;
import io.github.kronflow.core.spi.IdGenerator;
import io.github.kronflow.core.spi.NamespaceStore;

import java.util.List;
import java.util.Optional;

public class NamespaceService {
    private final NamespaceStore namespaceStore;
    private final IdGenerator idGenerator;

    public NamespaceService(NamespaceStore namespaceStore, IdGenerator idGenerator) {
        this.namespaceStore = namespaceStore;
        this.idGenerator = idGenerator;
    }

    public Namespace createNamespace(String name, String description, String createdBy, String updatedBy) {
        if (namespaceStore.existsNamespaceByName(name)) {
            throw new DuplicateNameException(String.format("Namespace %s already exists", name));
        }

        Namespace namespace = new Namespace(idGenerator.generateId(), name, description, createdBy, updatedBy);
        namespaceStore.create(namespace);
        return namespace;
    }

    public Namespace updateNameSpace(String namespaceId, String name, String description, String updatedBy) {
        Namespace namespace = namespaceStore.findById(namespaceId)
                .orElseThrow(() -> new IllegalStateException(String.format("Namespace %s doesn't exist", namespaceId)));

        if (!namespace.getName().equals(name) && namespaceStore.existsNamespaceByName(name)) {
            throw new DuplicateNameException(String.format("Namespace %s already exists", namespaceId));
        }

        namespace.rename(name);
        namespace.updateDescription(description);
        namespace.modifyUpdatedBy(updatedBy);

        namespaceStore.update(namespaceId, namespace);
        return namespace;
    }

    public Optional<Namespace> getNamespaceById(String id) {
        return namespaceStore.findById(id);
    }

    public List<Namespace> getAllNamespaces() {
        return namespaceStore.findAll();
    }

    public void deleteNamespace(String namespaceId) {
        if (!namespaceStore.existsById(namespaceId)) {
            throw new IllegalArgumentException(String.format("Namespace %s doesn't exist", namespaceId));
        }
        namespaceStore.deleteById(namespaceId);
    }
}
