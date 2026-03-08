package io.github.kronflow.core.exception;

public class JobExecutionException extends RuntimeException {
    public JobExecutionException(String message) {
        super(message);
    }

    public JobExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
