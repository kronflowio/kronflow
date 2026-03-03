package io.github.kronflow.core.model;

import java.time.Instant;
import java.util.Objects;

public abstract class BaseEntity {
    private final String id;
    private final String namespaceId;
    private final Instant createdAt;
    private Instant updatedAt;

    protected BaseEntity(String id, String namespaceId) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.namespaceId = Objects.requireNonNull(namespaceId, "namespaceId must not be null");
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() {
        return id;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    protected void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity other = (BaseEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + id + '\'' +
                ", namespaceId='" + namespaceId + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
