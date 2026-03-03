package io.github.kronflow.core.model;

import io.github.kronflow.core.model.enums.JobStatus;
import io.github.kronflow.core.model.value.CronExpression;
import io.github.kronflow.core.model.value.RetryPolicy;

import java.util.Objects;

public final class JobDefinition extends BaseEntity {
    private final String name;
    private final String description;
    private final String handlerName;
    private final String payload;           // raw JSON string, nullable
    private final CronExpression schedule;
    private RetryPolicy retryPolicy;
    private JobStatus status;

    public JobDefinition(String id,
                         String namespaceId,
                         String name,
                         String handlerName,
                         CronExpression schedule,
                         String description,
                         String payload,
                         RetryPolicy retryPolicy) {
        super(id, namespaceId);
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.handlerName = Objects.requireNonNull(handlerName, "handlerName must not be null");
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
        this.description = description;
        this.payload = payload;
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.DEFAULT;
        this.status = JobStatus.ACTIVE;
    }

//
//    JobDefinition(String id,
//                  String namespaceId,
//                  String name,
//                  String handlerName,
//                  CronExpression schedule,
//                  String description,
//                  String payload,
//                  RetryPolicy retryPolicy,
//                  JobStatus status,
//                  Instant createdAt,
//                  Instant updatedAt) {
//        super(id, namespaceId, createdAt, updatedAt);
//        this.name = Objects.requireNonNull(name, "name must not be null");
//        this.handlerName = Objects.requireNonNull(handlerName, "handlerName must not be null");
//        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
//        this.description = description;
//        this.payload = payload;
//        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.DEFAULT;
//        this.status = Objects.requireNonNull(status, "status must not be null");
//    }

    public void pause() {
        if (status.isDeleted())
            throw new IllegalStateException("Cannot pause a deleted job: " + getId());
        this.status = JobStatus.PAUSED;
        setUpdatedAt();
    }

    public void activate() {
        if (status.isDeleted())
            throw new IllegalStateException("Cannot activate a deleted job: " + getId());
        this.status = JobStatus.ACTIVE;
        setUpdatedAt();
    }

    public void delete() {
        this.status = JobStatus.DELETED;
        setUpdatedAt();
    }

    public void updateRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        setUpdatedAt();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getPayload() {
        return payload;
    }

    public CronExpression getSchedule() {
        return schedule;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public JobStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id='" + getId() + '\'' +
                ", namespaceId='" + getNamespaceId() + '\'' +
                ", name='" + name + '\'' +
                ", handlerName='" + handlerName + '\'' +
                ", schedule=" + schedule +
                ", status=" + status +
                ", retryPolicy=" + retryPolicy +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
