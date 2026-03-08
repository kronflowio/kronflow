package io.github.kronflow.core.spi;

import java.time.Instant;

public interface IdGenerator {
    /** Generates a unique entity ID. Default implementation uses UUID v7. */
    String generateId();
    /**
     * Generates a deterministic execution ID for idempotency.
     * Same {@code jobId} + {@code scheduledAt} must always produce the same ID.
     */
    String generateExecutionId(String jobId, Instant scheduledAt);
}
