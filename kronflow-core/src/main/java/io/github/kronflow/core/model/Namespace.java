package io.github.kronflow.core.model;

import java.time.Instant;
import java.util.Objects;

public final class Namespace {
    private final String id;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private boolean active;

    public Namespace(String id, String name, String description, String createdBy) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.createdBy = createdBy != null ? createdBy : "system";
        this.active = true;
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
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", active=" + active +
                '}';
    }
}
