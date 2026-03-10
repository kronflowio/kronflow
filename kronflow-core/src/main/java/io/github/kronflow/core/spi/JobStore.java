package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.JobDefinition;
import io.github.kronflow.core.model.Namespace;
import io.github.kronflow.core.model.enums.JobStatus;

import java.util.List;
import java.util.Optional;

public interface JobStore {
    void create(JobDefinition job);

    void update(JobDefinition job);

    Optional<JobDefinition> findById(String jobId);

    List<JobDefinition> finalAllByNamespace(String namespaceId);

    Optional<JobDefinition> findByNameSpaceAndName(String namespaceId, String name);

    List<JobDefinition> findByStatus(Namespace namespace, JobStatus status);

    void deleteById(String jobId);

    boolean existsById(String jobId);
}
