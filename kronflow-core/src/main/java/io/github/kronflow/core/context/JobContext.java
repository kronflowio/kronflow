package io.github.kronflow.core.context;

import java.time.Instant;

public class JobContext {
    private final String jobId;
    private final String namespaceId;
    private final Instant scheduledAt;
    private final Instant triggeredAt;
    private final int attemptNumber;
    private final String payload;

    public JobContext(
            String jobId,
            String namespaceId,
            Instant scheduledAt,
            Instant triggeredAt,
            int attemptNumber,
            String payload
    ) {
        this.jobId = jobId;
        this.namespaceId = namespaceId;
        this.scheduledAt = scheduledAt;
        this.triggeredAt = triggeredAt;
        this.attemptNumber = attemptNumber;
        this.payload = payload;
    }

    public String getJobId() {
        return jobId;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public Instant getTriggeredAt() {
        return triggeredAt;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "JobContext{" +
                "jobId='" + jobId + '\'' +
                ", namespaceId='" + namespaceId + '\'' +
                ", scheduledAt=" + scheduledAt +
                ", triggeredAt=" + triggeredAt +
                ", attemptNumber=" + attemptNumber +
                ", payload='" + payload + '\'' +
                '}';
    }
}
