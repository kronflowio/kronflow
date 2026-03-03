package io.github.kronflow.core.context;

import java.time.Instant;

public class JobContext {
    private String jobId;
    private Instant scheduledAt;
    private Instant triggeredAt;
    private int attemptNumber;
    private String payload;
}
