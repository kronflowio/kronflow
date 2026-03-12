package io.github.kronflow.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceTest {

    private static final String ID = "ns-1";
    private static final String NAME = "my-namespace";
    private static final String DESCRIPTION = "A test namespace";
    private static final String CREATED_BY = "user-1";
    private static final String UPDATED_BY = "user-2";

    private Namespace namespace;

    @BeforeEach
    void setUp() {
        namespace = new Namespace(ID, NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    class Construction {

        @Test
        void shouldInitialiseFieldsCorrectly() {
            assertAll(
                    () -> assertEquals(ID, namespace.getId()),
                    () -> assertEquals(NAME, namespace.getName()),
                    () -> assertEquals(DESCRIPTION, namespace.getDescription()),
                    () -> assertEquals(CREATED_BY, namespace.getCreatedBy()),
                    () -> assertEquals(UPDATED_BY, namespace.getUpdatedBy())
            );
        }

        @Test
        void shouldBeActiveOnCreation() {
            assertTrue(namespace.isActive());
        }

        @Test
        void shouldSetCreatedAtToNow() {
            Instant before = Instant.now().minusSeconds(1);
            Namespace ns = new Namespace(ID, NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);
            Instant after = Instant.now().plusSeconds(1);
            assertTrue(ns.getCreatedAt().isAfter(before));
            assertTrue(ns.getCreatedAt().isBefore(after));
        }

        @Test
        void shouldSetUpdatedAtEqualToCreatedAt() {
            assertEquals(namespace.getCreatedAt(), namespace.getUpdatedAt());
        }

        @Test
        void shouldDefaultCreatedByToSystemWhenNull() {
            Namespace ns = new Namespace(ID, NAME, DESCRIPTION, null, null);
            assertEquals("system", ns.getCreatedBy());
        }

        @Test
        void shouldDefaultUpdatedByToCreatedByWhenNull() {
            Namespace ns = new Namespace(ID, NAME, DESCRIPTION, CREATED_BY, null);
            assertEquals(CREATED_BY, ns.getUpdatedBy());
        }

        @Test
        void shouldDefaultUpdatedByToSystemWhenBothCreatedByAndUpdatedByAreNull() {
            Namespace ns = new Namespace(ID, NAME, DESCRIPTION, null, null);
            // createdBy defaults to "system", updatedBy defaults to createdBy
            assertEquals("system", ns.getUpdatedBy());
        }

        @Test
        void shouldAllowNullDescription() {
            Namespace ns = new Namespace(ID, NAME, null, CREATED_BY, UPDATED_BY);
            assertNull(ns.getDescription());
        }

        @Test
        void shouldThrowWhenIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new Namespace(null, NAME, DESCRIPTION, CREATED_BY, UPDATED_BY));
        }

        @Test
        void shouldThrowWhenNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new Namespace(ID, null, DESCRIPTION, CREATED_BY, UPDATED_BY));
        }
    }

    // -------------------------------------------------------------------------
    // rename()
    // -------------------------------------------------------------------------

    @Nested
    class Rename {

        @Test
        void shouldUpdateName() {
            namespace.rename("new-name");
            assertEquals("new-name", namespace.getName());
        }

        @Test
        void shouldUpdateUpdatedAt() throws InterruptedException {
            Instant before = namespace.getUpdatedAt();
            Thread.sleep(5);
            namespace.rename("new-name");
            assertTrue(namespace.getUpdatedAt().isAfter(before));
        }

        @Test
        void shouldAllowRenamingToNull() {
            // no null-guard in rename() — documents current behaviour
            assertDoesNotThrow(() -> namespace.rename(null));
            assertNull(namespace.getName());
        }
    }

    // -------------------------------------------------------------------------
    // updateDescription()
    // -------------------------------------------------------------------------

    @Nested
    class UpdateDescription {

        @Test
        void shouldUpdateDescription() {
            namespace.updateDescription("new description");
            assertEquals("new description", namespace.getDescription());
        }

        @Test
        void shouldAllowNullDescription() {
            namespace.updateDescription(null);
            assertNull(namespace.getDescription());
        }

        @Test
        void shouldUpdateUpdatedAt() throws InterruptedException {
            Instant before = namespace.getUpdatedAt();
            Thread.sleep(5);
            namespace.updateDescription("new description");
            assertTrue(namespace.getUpdatedAt().isAfter(before));
        }
    }

    // -------------------------------------------------------------------------
    // modifyUpdatedBy()
    // -------------------------------------------------------------------------

    @Nested
    class ModifyUpdatedBy {

        @Test
        void shouldUpdateUpdatedBy() {
            namespace.modifyUpdatedBy("admin");
            assertEquals("admin", namespace.getUpdatedBy());
        }

        @Test
        void shouldUpdateUpdatedAt() throws InterruptedException {
            Instant before = namespace.getUpdatedAt();
            Thread.sleep(5);
            namespace.modifyUpdatedBy("admin");
            assertTrue(namespace.getUpdatedAt().isAfter(before));
        }

        @Test
        void shouldAllowNullUpdatedBy() {
            assertDoesNotThrow(() -> namespace.modifyUpdatedBy(null));
            assertNull(namespace.getUpdatedBy());
        }
    }

    // -------------------------------------------------------------------------
    // activate()
    // -------------------------------------------------------------------------

    @Nested
    class Activate {

        @Test
        void shouldActivateDeactivatedNamespace() {
            namespace.deactivate();
            namespace.activate();
            assertTrue(namespace.isActive());
        }

        @Test
        void shouldBeIdempotentWhenAlreadyActive() {
            Instant updatedAt = namespace.getUpdatedAt();
            assertDoesNotThrow(() -> namespace.activate());
            // updatedAt must NOT change — no-op path
            assertEquals(updatedAt, namespace.getUpdatedAt());
        }

        @Test
        void shouldUpdateUpdatedAtOnActivate() throws InterruptedException {
            namespace.deactivate();
            Instant before = namespace.getUpdatedAt();
            Thread.sleep(5);
            namespace.activate();
            assertTrue(namespace.getUpdatedAt().isAfter(before));
        }
    }

    // -------------------------------------------------------------------------
    // deactivate()
    // -------------------------------------------------------------------------

    @Nested
    class Deactivate {

        @Test
        void shouldDeactivateActiveNamespace() {
            namespace.deactivate();
            assertFalse(namespace.isActive());
        }

        @Test
        void shouldBeIdempotentWhenAlreadyInactive() {
            namespace.deactivate();
            Instant updatedAt = namespace.getUpdatedAt();
            assertDoesNotThrow(() -> namespace.deactivate());
            // updatedAt must NOT change — no-op path
            assertEquals(updatedAt, namespace.getUpdatedAt());
        }

        @Test
        void shouldUpdateUpdatedAtOnDeactivate() throws InterruptedException {
            Instant before = namespace.getUpdatedAt();
            Thread.sleep(5);
            namespace.deactivate();
            assertTrue(namespace.getUpdatedAt().isAfter(before));
        }
    }

    // -------------------------------------------------------------------------
    // equals() and hashCode()
    // -------------------------------------------------------------------------

    @Nested
    class EqualsAndHashCode {

        @Test
        void shouldBeEqualWhenSameId() {
            Namespace other = new Namespace(ID, "different-name", null, "other-user", null);
            assertEquals(namespace, other);
        }

        @Test
        void shouldNotBeEqualWhenDifferentId() {
            Namespace other = new Namespace("ns-999", NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);
            assertNotEquals(namespace, other);
        }

        @Test
        void shouldBeEqualToItself() {
            assertEquals(namespace, namespace);
        }

        @Test
        void shouldNotBeEqualToNull() {
            assertNotEquals(null, namespace);
        }

        @Test
        void shouldNotBeEqualToDifferentType() {
            assertNotEquals("a string", namespace);
        }

        @Test
        void shouldHaveSameHashCodeForEqualObjects() {
            Namespace other = new Namespace(ID, "different-name", null, "other-user", null);
            assertEquals(namespace.hashCode(), other.hashCode());
        }

        @Test
        void shouldHaveDifferentHashCodeForDifferentIds() {
            Namespace other = new Namespace("ns-999", NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);
            assertNotEquals(namespace.hashCode(), other.hashCode());
        }
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Nested
    class ToStringTest {

        @Test
        void shouldContainKeyFields() {
            String result = namespace.toString();
            assertAll(
                    () -> assertTrue(result.contains(ID)),
                    () -> assertTrue(result.contains(NAME)),
                    () -> assertTrue(result.contains(DESCRIPTION)),
                    () -> assertTrue(result.contains(CREATED_BY)),
                    () -> assertTrue(result.contains(UPDATED_BY))
            );
        }
    }
}
