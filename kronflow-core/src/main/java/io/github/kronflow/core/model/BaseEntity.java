package io.github.kronflow.core.model;

import java.time.Instant;
import java.util.Objects;

public abstract class BaseEntity {
    private final String id;
    private final String namespaceId;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    protected BaseEntity(String id, String namespaceId, String createdBy) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.namespaceId = Objects.requireNonNull(namespaceId, "namespaceId must not be null");
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt; // on creation, updatedAt defaults to createdAt
        this.createdBy = createdBy != null ? createdBy : "system";
        this.updatedBy = createdBy; // on creation, updatedBy defaults to createdBy
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

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    protected void markUpdated(String updatedBy) {
        this.updatedAt = Instant.now();
        this.updatedBy = updatedBy != null ? updatedBy : "system";
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
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
