package io.github.kronflow.core.model.value;

import io.github.kronflow.core.exception.InvalidCronExpressionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CronExpressionTest {

    // ----------------- construction tests --------------------------------------------
    @Test
    void shouldCreateCronExpression_whenExpressionIsValid() {
        CronExpression cron = new CronExpression("0 */5 * * * ?");
        assertEquals("0 */5 * * * ?", cron.getExpression());
    }

    @Test
    void shouldTrimWhitespace_whenExpressionHasLeadingTrailingSpaces() {
        CronExpression cron = new CronExpression("  0 */5 * * * ?  ");
        assertEquals("0 */5 * * * ?", cron.getExpression());
    }

    @Test
    void shouldThrowNullPointerException_whenExpressionIsNull() {
        assertThrows(NullPointerException.class, () -> new CronExpression(null));
    }

    // ------------------ field count validation tests ---------------------------------
    @Test
    void shouldThrow_whenExpressionHasFewerThan6Fields() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class, () -> new CronExpression("0 */5 * *")
        );
        assertTrue(ex.getMessage().contains("6-field cron expression"));
    }

    @Test
    void shouldThrow_whenExpressionHasMoreThan6Fields() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class, () -> new CronExpression("0 */5 * * * ? 2025")
        );
        assertTrue(ex.getMessage().contains("6-field cron expression"));
    }

    // ---------------------- field range validation tests -------------------------------
    @ParameterizedTest
    @MethodSource("invalidFieldValues")
    void shouldThrow_whenFieldValueOutOfRange(String expr, String expectedField) {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class,
                () -> new CronExpression(expr)
        );
        assertTrue(ex.getMessage().contains(expectedField));
        assertTrue(ex.getMessage().contains("out of range"));
    }

    static Stream<Arguments> invalidFieldValues() {
        return Stream.of(
                Arguments.of("60 * * * * ?", "seconds"),
                Arguments.of("0 60 * * * ?", "minutes"),
                Arguments.of("0 0 24 * * ?", "hours"),
                Arguments.of("0 0 0 32 * ?", "day-of-month"),
                Arguments.of("0 0 0 1 13 ?", "month"),
                Arguments.of("0 0 0 * * 8", "day-of-week")
        );
    }

    // --------------------- valid numeric values tests ---------------------------------------
    @Test
    void shouldAcceptBoundaryValues() {
        assertDoesNotThrow(() -> new CronExpression("0 0 0 1 1 0"));    // min values
        assertDoesNotThrow(() -> new CronExpression("59 59 23 31 12 7")); // max values
    }

    // --------------------- wildcards and special chars tests --------------------------------
    @ParameterizedTest
    @ValueSource(strings = {
            "* * * * * *",
            "? * * * * *",
            "* * * * * ?"
    })
    void shouldAcceptWildcardsAndQuestionMark(String expr) {
        assertDoesNotThrow(() -> new CronExpression(expr));
    }

    // ---------------------- step tests -----------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "*/1 * * * * ?",
            "*/5 * * * * ?",
            "0/15 * * * * ?",
            "30/10 * * * * ?"
    })
    void shouldAcceptStepNotation(String expr) {
        assertDoesNotThrow(() -> new CronExpression(expr));
    }

    @Test
    void shouldRejectStepWithInvalidBase() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class, () -> new CronExpression("60/5 * * * * ?")
        );
        assertTrue(ex.getMessage().contains("seconds"));
    }

    // --------------------- range tests-------------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "0-30 * * * * ?",
            "10-20 * * * * ?"
    })
    void shouldAcceptRangeNotation(String expr) {
        assertDoesNotThrow(() -> new CronExpression(expr));
    }

    @Test
    void shouldRejectRangeWithInvalidBounds() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class,
                () -> new CronExpression("0-60 * * * * ?")
        );
        assertTrue(ex.getMessage().contains("seconds"));
    }

    // ------------------------- lists tests -----------------------------------------------------

    @ParameterizedTest
    @ValueSource(strings = {
            "0,15,30,45 * * * * ?",
            "0,30 * * * * ?"
    })
    void shouldAcceptListNotation(String expr) {
        assertDoesNotThrow(() -> new CronExpression(expr));
    }

    @Test
    void shouldRejectListWithInvalidValue() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class,
                () -> new CronExpression("60,30 * * * * ?")
        );
        assertTrue(ex.getMessage().contains("seconds"));
    }

    // ------------------- non-numeric values tests ------------------------------------------
    @Test
    void shouldRejectNonNumericPlainValue() {
        InvalidCronExpressionException ex = assertThrows(
                InvalidCronExpressionException.class,
                () -> new CronExpression("abc * * * * ?")
        );
        assertTrue(ex.getMessage().contains("seconds"));
        assertTrue(ex.getMessage().contains("invalid value"));
    }

    // ----------------------- equals and hashCode tests -------------------------------------
    @Test
    void shouldBeEqual_whenExpressionsAreIdentical() {
        CronExpression a = new CronExpression("0 */5 * * * ?");
        CronExpression b = new CronExpression("0 */5 * * * ?");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void shouldNotBeEqual_whenExpressionsAreDifferent() {
        CronExpression a = new CronExpression("0 */5 * * * ?");
        CronExpression b = new CronExpression("0 */10 * * * ?");
        assertNotEquals(a, b);
    }

    @Test
    void shouldBeEqualToItself() {
        CronExpression cron = new CronExpression("0 */5 * * * ?");
        assertEquals(cron, cron);
    }

    @Test
    void shouldNotBeEqualToNull() {
        CronExpression cron = new CronExpression("0 */5 * * * ?");
        assertNotEquals(null, cron);
    }

    // -------------------- toString tests ---------------------------------------------------
    @Test
    void shouldReturnExpression_fromToString() {
        CronExpression cron = new CronExpression("0 */5 * * * ?");
        assertEquals("0 */5 * * * ?", cron.toString());
    }
}
