package io.github.kronflow.core.spi;

import java.time.Instant;

public interface IdGenerator {
    String generateId();  // entity IDs
    String generateExecutionId(String jobName, Instant scheduledAt); // deterministic idempotency key
}
