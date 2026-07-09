package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.JobDefinition;
import io.github.kronflow.core.model.enums.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobStore {
    void create(JobDefinition job);

    void update(JobDefinition job);

    Optional<JobDefinition> findById(String jobId);

    boolean existsById(String jobId);

    List<JobDefinition> findAllByNamespace(String namespaceId);

    Optional<JobDefinition> findByNamespaceAndName(String namespaceId, String name);

    List<JobDefinition> findByStatus(String namespaceId, JobStatus status);

    /**
     * Critical for the JobSchedulerEngine polling loop.
     * Finds jobs where status is ACTIVE and nextFireTime <= NOW.
     *
     * @param limit Max number of jobs to return in one batch to prevent OOM
     */
    List<JobDefinition> findDueJobs(int limit);

    void deleteById(String jobId);
}