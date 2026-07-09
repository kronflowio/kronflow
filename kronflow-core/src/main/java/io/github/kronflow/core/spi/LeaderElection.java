package io.github.kronflow.core.spi;

public interface LeaderElection {
    /**
     * Attempts to acquire or renew leadership.
     * @return true if this instance is now the leader, false otherwise.
     */
    boolean acquireLease();

    /**
     * Checks if this instance currently holds a valid leadership lease.
     */
    boolean isLeader();

    /**
     * Voluntarily steps down from leadership (useful for graceful shutdown).
     */
    void releaseLease();
}