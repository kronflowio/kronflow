package io.github.kronflow.core.model.value;

import java.util.Objects;

public final class RetryPolicy {

    public static final RetryPolicy NO_RETRY = new RetryPolicy(0, 0, 1);
    public static final RetryPolicy DEFAULT = new RetryPolicy(3, 1000, 2);

    private final int maxAttempts;       // total attempts including the first
    private final long initialDelayMs;   // delay before first retry
    private final double backoffMultiplier; // exponential backoff factor

    public RetryPolicy(int maxAttempts, long initialDelayMs, double backoffMultiplier) {
        if (maxAttempts < 0)
            throw new IllegalArgumentException("maxAttempts must be >= 0");
        if (initialDelayMs < 0)
            throw new IllegalArgumentException("initialDelayMs must be >= 0");
        if (backoffMultiplier < 1.0)
            throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");

        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.backoffMultiplier = backoffMultiplier;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public double getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public long delayForAttempt(int attemptNumber) {
        if (attemptNumber <= 1) return initialDelayMs;
        return (long) (initialDelayMs * Math.pow(backoffMultiplier, attemptNumber - 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RetryPolicy other)) return false;
        return maxAttempts == other.maxAttempts
                && initialDelayMs == other.initialDelayMs
                && Double.compare(backoffMultiplier, other.backoffMultiplier) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAttempts, initialDelayMs, backoffMultiplier);
    }

    @Override
    public String toString() {
        return "RetryPolicy{maxAttempts=" + maxAttempts +
                ", initialDelayMs=" + initialDelayMs +
                ", backoffMultiplier=" + backoffMultiplier + "}";
    }
}
