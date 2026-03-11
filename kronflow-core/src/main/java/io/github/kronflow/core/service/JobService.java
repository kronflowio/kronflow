package io.github.kronflow.core.service;

import io.github.kronflow.core.model.JobDefinition;
import io.github.kronflow.core.model.enums.JobStatus;
import io.github.kronflow.core.model.value.CronExpression;
import io.github.kronflow.core.model.value.RetryPolicy;
import io.github.kronflow.core.spi.IdGenerator;
import io.github.kronflow.core.spi.JobStore;
import io.github.kronflow.core.spi.NamespaceStore;

import java.util.List;
import java.util.Optional;

public class JobService {
    private final JobStore jobStore;
    private final NamespaceStore namespaceStore;
    private final IdGenerator idGenerator;

    public JobService(JobStore store, NamespaceStore namespaceStore, IdGenerator idGenerator) {
        this.jobStore = store;
        this.namespaceStore = namespaceStore;
        this.idGenerator = idGenerator;
    }

    public JobDefinition createJob(
            String namespaceId,
            String name,
            String description,
            String handlerName,
            String payload,
            CronExpression schedule,
            RetryPolicy retryPolicy,
            String createdBy
    ) {
        if (namespaceStore.findById(namespaceId).isEmpty()) {
            throw new IllegalArgumentException(String.format("namespace %s does not exist", namespaceId));
        }

        if (jobStore.findByNamespaceAndName(namespaceId, name).isPresent()) {
            throw new IllegalArgumentException(String.format("job with the same name %s already exists", name));
        }

        JobDefinition job = new JobDefinition(
                idGenerator.generateId(),
                namespaceId,
                createdBy,
                name,
                description,
                handlerName,
                schedule,
                payload,
                retryPolicy
        );
        jobStore.create(job);
        return job;
    }

    public JobDefinition updateJob(String id, JobDefinition req, String updatedBy) {
        JobDefinition job = jobStore.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(String.format("job %s does not exist", id)));

        if (job.getStatus() == JobStatus.DELETED) {
            throw new IllegalArgumentException(String.format("Cannot update deleted job: %s", id));
        }
        job.rename(req.getName(), updatedBy);
        job.updateDescription(req.getDescription(), updatedBy);
        job.updateHandlerName(req.getHandlerName(), updatedBy);
        job.updatePayload(req.getPayload(), updatedBy);
        job.updateSchedule(req.getSchedule(), updatedBy);
        job.updateRetryPolicy(req.getRetryPolicy(), updatedBy);

        switch (req.getStatus()) {
            case ACTIVE -> job.activate(updatedBy);
            case PAUSED -> job.pause(updatedBy);
            case DELETED -> job.delete(updatedBy);
        }
        jobStore.update(job);
        return job;
    }

    public Optional<JobDefinition> getJobById(String jobId) {
        return jobStore.findById(jobId);
    }

    public List<JobDefinition> getAllJobs(String namespaceId) {
        return jobStore.findAllByNamespace(namespaceId);
    }

    public void deleteJob(String jobId) {
        if (!jobStore.existsById(jobId)) {
            throw new IllegalArgumentException(String.format("job %s does not exist", jobId));
        }
        jobStore.deleteById(jobId);
    }

}
