package io.github.kronflow.core.model;

public enum ExecutionStatus {
    PENDING,    // scheduled, but not yet picket up
    RUNNING,    // currently executing
    SUCCESS,    // completed successfully
    FAILED,     // failed, eligible for retry
    SKIPPED,    // duplicate detected and hence, skipped
    DEAD;       // error after retry, in dead letter queue (DLQ)
}
