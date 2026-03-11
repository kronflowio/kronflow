package io.github.kronflow.core.model.enums;

public enum JobStatus {
    ACTIVE,
    PAUSED,
    DELETED;

    public boolean isDeleted() {
        return this == DELETED;
    }

    public boolean isPaused() {
        return this == PAUSED;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
