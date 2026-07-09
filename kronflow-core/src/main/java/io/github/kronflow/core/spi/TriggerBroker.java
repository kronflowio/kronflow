package io.github.kronflow.core.spi;

import io.github.kronflow.core.model.JobDefinition;

public interface TriggerBroker {
    /**
     * Dispatches a due job for execution.
     * In V1 (embedded), this routes to a local worker pool.
     * In V4 (distributed), this publishes to Kafka.
     */
    void dispatch(JobDefinition job);
}