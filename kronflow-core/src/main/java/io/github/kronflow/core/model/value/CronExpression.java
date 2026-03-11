package io.github.kronflow.core.model.value;

import io.github.kronflow.core.exception.InvalidCronExpressionException;

import java.util.Objects;

public final class CronExpression {

    private static final int FIELD_COUNT = 6;

    // field indices
    private static final int IDX_SECONDS = 0;
    private static final int IDX_MINUTES = 1;
    private static final int IDX_HOURS   = 2;
    private static final int IDX_DOM     = 3;
    private static final int IDX_MONTH   = 4;
    private static final int IDX_DOW     = 5;

    private final String expression;

    public CronExpression(String expression) {
        Objects.requireNonNull(expression, "cron expression must not be null");
        this.expression = validate(expression.trim());
    }

    private String validate(String expr) {
        String[] parts = expr.split("\\s+");

        if (parts.length != FIELD_COUNT) {
            throw new InvalidCronExpressionException(
                    "Kronflow requires a 6-field cron expression " +
                            "(sec min hr dom mon dow), got " + parts.length + " fields in [" + expr + "]"
            );
        }

        validateRangeField(parts[IDX_SECONDS], 0,  59, "seconds");
        validateRangeField(parts[IDX_MINUTES], 0,  59, "minutes");
        validateRangeField(parts[IDX_HOURS],   0,  23, "hours");
        validateRangeField(parts[IDX_DOM],     1,  31, "day-of-month");
        validateRangeField(parts[IDX_MONTH],   1,  12, "month");
        validateRangeField(parts[IDX_DOW],     0,   7, "day-of-week");

        return expr;
    }

    /**
     * Validates a single cron field against an allowed numeric range.
     * Wildcards (* and ?), steps (slash), ranges (dash), and lists (comma)
     * are accepted without deep validation — structural correctness is
     * enforced at execution time by the trigger engine.
     */
    private void validateRangeField(String field, int min, int max, String fieldName) {
        if ("*".equals(field) || "?".equals(field)) return;

        // steps: */5 or 0/5 — validate the base if numeric
        if (field.contains("/")) {
            String base = field.split("/")[0];
            if (!"*".equals(base)) validateNumeric(base, min, max, fieldName);
            return;
        }

        // ranges: 1-5 — validate both bounds
        if (field.contains("-")) {
            String[] bounds = field.split("-");
            validateNumeric(bounds[0], min, max, fieldName);
            validateNumeric(bounds[1], min, max, fieldName);
            return;
        }

        // lists: 1,2,3 — validate each value
        if (field.contains(",")) {
            for (String part : field.split(",")) {
                validateNumeric(part.trim(), min, max, fieldName);
            }
            return;
        }

        // plain numeric
        validateNumeric(field, min, max, fieldName);
    }

    private void validateNumeric(String value, int min, int max, String fieldName) {
        try {
            int num = Integer.parseInt(value);
            if (num < min || num > max) {
                throw new InvalidCronExpressionException(
                        "Field '" + fieldName + "' value " + num + " is out of range [" + min + "-" + max + "]"
                );
            }
        } catch (NumberFormatException e) {
            throw new InvalidCronExpressionException(
                    "Field '" + fieldName + "' contains invalid value: '" + value + "'"
            );
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
