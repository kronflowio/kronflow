package io.github.kronflow.core.model.value;

import io.github.kronflow.core.exception.InvalidCronExpressionException;

import java.util.Objects;
import java.util.regex.Pattern;

public final class CronExpression {

    // Matches 6-field (sec min hr dom mon dow) or 7-field (+ year) cron expressions
    private static final Pattern CRON_PATTERN = Pattern.compile(
            "(@(annually|yearly|monthly|weekly|daily|hourly|reboot))" +
                    "|((@every)(\\s+)(\\d+(ns|us|µs|ms|s|m|h))+)" +
                    "|(((\\d+,)+\\d+|(\\d+(\\/|-)\\d+)|\\d+|\\*|\\?) " +
                    "{5,6}" +
                    "(\\d+,)+\\d+|(\\d+(\\/|-)\\d+)|\\d+|\\*|\\?)"
    );

    private final String expression;

    public CronExpression(String expression) {
        Objects.requireNonNull(expression, "cron expression must not be null");
        this.expression = validate(expression.trim());
    }

    private String validate(String expr) {
        String[] parts = expr.split("\\s+");
        if (parts.length < 5 || parts.length > 7) {
            throw new InvalidCronExpressionException(
                    "Cron expression must have 5, 6, or 7 fields, got: " + parts.length + " in [" + expr + "]"
            );
        }
        // field-level range validation
        validateField(parts[0], 0, 59, "seconds/minutes");
        validateField(parts[1], 0, 59, "minutes");
        validateField(parts[2], 0, 23, "hours");
        // TODO: dom and month are structural — skip deep range check for now
        return expr;
    }

    private void validateField(String field, int min, int max, String fieldName) {
        if ("*".equals(field) || "?".equals(field)) return;
        try {
            int value = Integer.parseInt(field);
            if (value < min || value > max) {
                throw new InvalidCronExpressionException(
                        "Field '" + fieldName + "' value " + value +
                                " is out of range [" + min + "-" + max + "]"
                );
            }
        } catch (NumberFormatException e) {
            // TODO: slashes and commas are valid - skip for now, deeper validation can be added later
        }
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CronExpression other)) return false;
        return Objects.equals(expression, other.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}
