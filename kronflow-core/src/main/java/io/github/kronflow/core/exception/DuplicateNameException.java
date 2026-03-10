package io.github.kronflow.core.exception;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String message) {
        super(message);
    }

    public DuplicateNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
