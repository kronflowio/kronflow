package io.github.kronflow.core.model.value;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RetryPolicyTest {

    // -------------------------------------------------------------------------
    // Construction validation
    // -------------------------------------------------------------------------

    @Nested
    class Construction {

        @Test
        void shouldCreateValidRetryPolicy() {
            RetryPolicy policy = new RetryPolicy(3, 1000, 2.0);

            assertAll(
                    () -> assertEquals(3, policy.getMaxAttempts()),
                    () -> assertEquals(1000, policy.getInitialDelayMs()),
                    () -> assertEquals(2.0, policy.getBackoffMultiplier())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -5})
        void shouldThrowWhenMaxAttemptsNegative(int maxAttempts) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RetryPolicy(maxAttempts, 1000, 2.0));
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -100L})
        void shouldThrowWhenInitialDelayNegative(long initialDelayMs) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RetryPolicy(3, initialDelayMs, 2.0));
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.5, 0.9, -1.0})
        void shouldThrowWhenBackoffMultiplierLessThanOne(double backoffMultiplier) {
            assertThrows(IllegalArgumentException.class,
                    () -> new RetryPolicy(3, 1000, backoffMultiplier));
        }

        @Test
        void shouldAllowZeroMaxAttempts() {
            assertDoesNotThrow(() -> new RetryPolicy(0, 1000, 2.0));
        }

        @Test
        void shouldAllowZeroInitialDelay() {
            assertDoesNotThrow(() -> new RetryPolicy(3, 0, 2.0));
        }

        @Test
        void shouldAllowBackoffMultiplierExactlyOne() {
            assertDoesNotThrow(() -> new RetryPolicy(3, 1000, 1.0));
        }
    }

    // -------------------------------------------------------------------------
    // delayForAttempt()
    // -------------------------------------------------------------------------

    @Nested
    class DelayForAttempt {

        @Test
        void shouldReturnInitialDelayForFirstAttempt() {
            RetryPolicy policy = new RetryPolicy(3, 1000, 2.0);
            assertEquals(1000, policy.delayForAttempt(1));
        }

        @Test
        void shouldReturnInitialDelayForAttemptZero() {
            // edge case — attemptNumber <= 1 returns initialDelay
            RetryPolicy policy = new RetryPolicy(3, 1000, 2.0);
            assertEquals(1000, policy.delayForAttempt(0));
        }

        @ParameterizedTest
        @MethodSource("backoffCalculationProvider")
        void shouldCalculateExponentialBackoff(int attempt, long expectedDelay) {
            RetryPolicy policy = new RetryPolicy(3, 1000, 2.0);
            assertEquals(expectedDelay, policy.delayForAttempt(attempt));
        }

        static Stream<Arguments> backoffCalculationProvider() {
            return Stream.of(
                    Arguments.of(2, 1000 * 2L),    // attempt 2: 1000 * 2^1
                    Arguments.of(3, 1000 * 4L),    // attempt 3: 1000 * 2^2
                    Arguments.of(4, 1000 * 8L),    // attempt 4: 1000 * 2^3
                    Arguments.of(10, 1000 * 512L)  // attempt 10: 1000 * 2^9
            );
        }

        @Test
        void shouldWorkWithBackoffMultiplierOne() {
            RetryPolicy policy = new RetryPolicy(3, 1000, 1.0);
            assertAll(
                    () -> assertEquals(1000, policy.delayForAttempt(1)),
                    () -> assertEquals(1000, policy.delayForAttempt(2)),
                    () -> assertEquals(1000, policy.delayForAttempt(3))
            );
        }

        @Test
        void shouldWorkWithZeroInitialDelay() {
            RetryPolicy policy = new RetryPolicy(3, 0, 2.0);
            assertAll(
                    () -> assertEquals(0, policy.delayForAttempt(1)),
                    () -> assertEquals(0, policy.delayForAttempt(2)),
                    () -> assertEquals(0, policy.delayForAttempt(3))
            );
        }

        @Test
        void shouldHandleLargeAttemptNumbersWithoutOverflow() {
            // maxAttempts typically limits this, but delayForAttempt doesn't check it
            RetryPolicy policy = new RetryPolicy(3, 1000, 1.5);
            long delay = policy.delayForAttempt(30); // 1000 * 1.5^29
            // Just verify it doesn't throw — exact value depends on double precision
            assertTrue(delay > 0);
        }
    }

    // -------------------------------------------------------------------------
    // Predefined policies
    // -------------------------------------------------------------------------

    @Nested
    class PredefinedPolicies {

        @Test
        void shouldVerifyNO_RETRY() {
            assertAll(
                    () -> assertEquals(0, RetryPolicy.NO_RETRY.getMaxAttempts()),
                    () -> assertEquals(0, RetryPolicy.NO_RETRY.getInitialDelayMs()),
                    () -> assertEquals(1.0, RetryPolicy.NO_RETRY.getBackoffMultiplier()),
                    () -> assertEquals(0, RetryPolicy.NO_RETRY.delayForAttempt(1))
            );
        }

        @Test
        void shouldVerifyDEFAULT() {
            assertAll(
                    () -> assertEquals(3, RetryPolicy.DEFAULT.getMaxAttempts()),
                    () -> assertEquals(1000, RetryPolicy.DEFAULT.getInitialDelayMs()),
                    () -> assertEquals(2.0, RetryPolicy.DEFAULT.getBackoffMultiplier()),
                    () -> assertEquals(1000, RetryPolicy.DEFAULT.delayForAttempt(1)),
                    () -> assertEquals(2000, RetryPolicy.DEFAULT.delayForAttempt(2)),
                    () -> assertEquals(4000, RetryPolicy.DEFAULT.delayForAttempt(3))
            );
        }
    }

    // -------------------------------------------------------------------------
    // equals() and hashCode()
    // -------------------------------------------------------------------------

    @Nested
    class EqualsAndHashCode {

        private final RetryPolicy base = new RetryPolicy(3, 1000, 2.0);

        @Test
        void shouldBeEqualToItself() {
            assertEquals(base, base);
        }

        @Test
        void shouldBeEqualWhenAllFieldsMatch() {
            RetryPolicy other = new RetryPolicy(3, 1000, 2.0);
            assertEquals(base, other);
        }

        @Test
        void shouldNotBeEqualWhenMaxAttemptsDiffers() {
            RetryPolicy other = new RetryPolicy(5, 1000, 2.0);
            assertNotEquals(base, other);
        }

        @Test
        void shouldNotBeEqualWhenInitialDelayDiffers() {
            RetryPolicy other = new RetryPolicy(3, 2000, 2.0);
            assertNotEquals(base, other);
        }

        @Test
        void shouldNotBeEqualWhenBackoffMultiplierDiffers() {
            RetryPolicy other = new RetryPolicy(3, 1000, 1.5);
            assertNotEquals(base, other);
        }

        @Test
        void shouldNotBeEqualToNull() {
            assertNotEquals(base, null);
        }

        @Test
        void shouldNotBeEqualToDifferentType() {
            assertNotEquals(base, "not a retry policy");
        }

        @Test
        void shouldHaveSameHashCodeForEqualObjects() {
            RetryPolicy other = new RetryPolicy(3, 1000, 2.0);
            assertEquals(base.hashCode(), other.hashCode());
        }

        @Test
        void shouldHaveDifferentHashCodeForDifferentObjects() {
            RetryPolicy other = new RetryPolicy(5, 1000, 2.0);
            assertNotEquals(base.hashCode(), other.hashCode());
        }
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Nested
    class ToStringTest {

        @Test
        void shouldContainAllFields() {
            RetryPolicy policy = new RetryPolicy(3, 1000, 2.0);
            String result = policy.toString();

            assertAll(
                    () -> assertTrue(result.contains("maxAttempts=3")),
                    () -> assertTrue(result.contains("initialDelayMs=1000")),
                    () -> assertTrue(result.contains("backoffMultiplier=2.0"))
            );
        }
    }
}
