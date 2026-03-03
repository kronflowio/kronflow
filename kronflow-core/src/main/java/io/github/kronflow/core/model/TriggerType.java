package io.github.kronflow.core.model;

public enum TriggerType {
    CRON,           // standard cron expression
    FIXED_RATE,     // every N milliseconds
    ONE_TIME,       // fire once at a specific future time
    EVENT_DRIVEN    // placeholder, for future use
}
