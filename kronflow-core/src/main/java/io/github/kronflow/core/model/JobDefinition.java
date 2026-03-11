package io.github.kronflow.core.model;

import io.github.kronflow.core.model.enums.JobStatus;
import io.github.kronflow.core.model.value.CronExpression;
import io.github.kronflow.core.model.value.RetryPolicy;

import java.util.Objects;

public final class JobDefinition extends BaseEntity {
    private String name;
    private String description;
    private String handlerName;
    private String payload;           // raw JSON string, nullable
    private CronExpression schedule;
    private RetryPolicy retryPolicy;
    private JobStatus status;

    public JobDefinition(String id,
                         String namespaceId,
                         String createdBy,
                         String name,
                         String description,
                         String handlerName,
                         CronExpression schedule,
                         String payload,
                         RetryPolicy retryPolicy) {
        super(id, namespaceId, createdBy);
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.handlerName = Objects.requireNonNull(handlerName, "handlerName must not be null");
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
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

    public void pause(String updatedBy) {
        if (status.isDeleted())
            throw new IllegalStateException("Cannot pause a deleted job: " + getId());
        if (status.isPaused()) {
            return;
        }
        status = JobStatus.PAUSED;
        markUpdated(updatedBy);
    }

    public void activate(String updatedBy) {
        if (status.isDeleted())
            throw new IllegalStateException("Cannot activate a deleted job: " + getId());
        if (status.isActive()) {
            return;
        }
        this.status = JobStatus.ACTIVE;
        markUpdated(updatedBy);
    }

    public void delete(String updatedBy) {
        if (status.isDeleted()) {
            throw new IllegalStateException("Cannot activate a deleted job: " + getId());
        }
        this.status = JobStatus.DELETED;
        markUpdated(updatedBy);
    }

    public void rename(String name, String updatedBy) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        markUpdated(updatedBy);
    }

    public void updateDescription(String description, String updatedBy) {
        this.description = description;
        markUpdated(updatedBy);
    }

    public void updateHandlerName(String handlerName, String updatedBy) {
        this.handlerName = Objects.requireNonNull(handlerName, "handlerName must not be null");
        markUpdated(updatedBy);
    }

    public void updatePayload(String payload, String updatedBy) {
        this.payload = payload;
        markUpdated(updatedBy);
    }

    public void updateSchedule(CronExpression schedule, String updatedBy) {
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
        markUpdated(updatedBy);
    }

    public void updateRetryPolicy(RetryPolicy retryPolicy, String updatedBy) {
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy must not be null");
        markUpdated(updatedBy);
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
                ", createdBy=" + getCreatedBy() +
                ", updatedBy=" + getUpdatedBy() +
                '}';
    }
}
