package io.github.kronflow.core.spi;

import io.github.kronflow.core.context.JobContext;
import io.github.kronflow.core.exception.JobExecutionException;

public interface Job {
    void execute(JobContext ctx) throws JobExecutionException;
}
