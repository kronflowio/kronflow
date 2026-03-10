package io.github.kronflow.core.model;

import java.time.Instant;
import java.util.Objects;

public final class Namespace {
    private final String id;
    private final Instant createdAt;
    private final String createdBy;
    private String name;
    private String description;
    private String updatedBy;
    private Instant updatedAt;
    private boolean active;

    public Namespace(String id, String name, String description, String createdBy, String updatedBy) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.createdAt = Instant.now();
        this.createdBy = createdBy != null ? createdBy : "system";
        this.updatedBy = updatedBy != null ? updatedBy : createdBy;
        this.updatedAt = this.createdAt;
        this.active = true;
    }

    public void rename(String newName) {
        this.name = newName;
        setUpdatedAt();
    }

    public void updateDescription(String description) {
        this.description = description;
        setUpdatedAt();
    }

    public void modifyUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        setUpdatedAt();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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

    public boolean isActive() {
        return active;
    }

    private void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public void activate() {
        if (this.active) {
            return;
        }
        this.active = true;
        setUpdatedAt();
    }

    public void deactivate() {
        if (!this.active) {
            return;
        }
        this.active = false;
        setUpdatedAt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Namespace other)) return false;
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
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", active=" + active +
                '}';
    }
}
