package io.github.kronflow.core.model;

import static org.junit.jupiter.api.Assertions.*;

import io.github.kronflow.core.model.enums.JobStatus;
import io.github.kronflow.core.model.value.CronExpression;
import io.github.kronflow.core.model.value.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JobDefinitionTest {

    private static final String ID           = "job-1";
    private static final String NAMESPACE_ID = "ns-1";
    private static final String CREATED_BY   = "user-1";
    private static final String NAME         = "my-job";
    private static final String DESCRIPTION  = "A test job";
    private static final String HANDLER      = "myHandler";
    private static final String PAYLOAD      = "{\"key\":\"value\"}";

    private CronExpression schedule;
    private RetryPolicy retryPolicy;
    private JobDefinition job;

    @BeforeEach
    void setUp() {
        schedule    = new CronExpression("0 * * * * *");
        retryPolicy = new RetryPolicy(3, 1000L, 2.0);
        job = new JobDefinition(
                ID, NAMESPACE_ID, CREATED_BY,
                NAME, DESCRIPTION, HANDLER,
                schedule, PAYLOAD, retryPolicy
        );
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Nested
    class Construction {

        @Test
        void shouldInitialiseFieldsCorrectly() {
            assertEquals(ID,           job.getId());
            assertEquals(NAMESPACE_ID, job.getNamespaceId());
            assertEquals(NAME,         job.getName());
            assertEquals(DESCRIPTION,  job.getDescription());
            assertEquals(HANDLER,      job.getHandlerName());
            assertEquals(PAYLOAD,      job.getPayload());
            assertEquals(schedule,     job.getSchedule());
            assertEquals(retryPolicy,  job.getRetryPolicy());
        }

        @Test
        void shouldSetStatusToActiveOnCreation() {
            assertEquals(JobStatus.ACTIVE, job.getStatus());
        }

        @Test
        void shouldUseDefaultRetryPolicyWhenNullPassed() {
            JobDefinition j = new JobDefinition(
                    ID, NAMESPACE_ID, CREATED_BY,
                    NAME, DESCRIPTION, HANDLER,
                    schedule, PAYLOAD, null
            );
            assertEquals(RetryPolicy.DEFAULT, j.getRetryPolicy());
        }

        @Test
        void shouldAllowNullPayload() {
            JobDefinition j = new JobDefinition(
                    ID, NAMESPACE_ID, CREATED_BY,
                    NAME, DESCRIPTION, HANDLER,
                    schedule, null, retryPolicy
            );
            assertNull(j.getPayload());
        }

        @Test
        void shouldAllowNullDescription() {
            JobDefinition j = new JobDefinition(
                    ID, NAMESPACE_ID, CREATED_BY,
                    NAME, null, HANDLER,
                    schedule, PAYLOAD, retryPolicy
            );
            assertNull(j.getDescription());
        }

        @Test
        void shouldThrowWhenNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new JobDefinition(ID, NAMESPACE_ID, CREATED_BY,
                            null, DESCRIPTION, HANDLER,
                            schedule, PAYLOAD, retryPolicy)
            );
        }

        @Test
        void shouldThrowWhenHandlerNameIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new JobDefinition(ID, NAMESPACE_ID, CREATED_BY,
                            NAME, DESCRIPTION, null,
                            schedule, PAYLOAD, retryPolicy)
            );
        }

        @Test
        void shouldThrowWhenScheduleIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new JobDefinition(ID, NAMESPACE_ID, CREATED_BY,
                            NAME, DESCRIPTION, HANDLER,
                            null, PAYLOAD, retryPolicy)
            );
        }
    }

    // -------------------------------------------------------------------------
    // pause()
    // -------------------------------------------------------------------------

    @Nested
    class Pause {

        @Test
        void shouldPauseActiveJob() {
            job.pause("admin");
            assertEquals(JobStatus.PAUSED, job.getStatus());
        }

        @Test
        void shouldBeIdempotentWhenAlreadyPaused() {
            job.pause("admin");
            assertDoesNotThrow(() -> job.pause("admin"));
            assertEquals(JobStatus.PAUSED, job.getStatus());
        }

        @Test
        void shouldThrowWhenPausingDeletedJob() {
            job.delete("admin");
            assertThrows(IllegalStateException.class, () -> job.pause("admin"));
        }

        @Test
        void shouldUpdateUpdatedByOnPause() {
            job.pause("admin");
            assertEquals("admin", job.getUpdatedBy());
        }
    }

    // -------------------------------------------------------------------------
    // activate()
    // -------------------------------------------------------------------------

    @Nested
    class Activate {

        @Test
        void shouldActivatePausedJob() {
            job.pause("admin");
            job.activate("admin");
            assertEquals(JobStatus.ACTIVE, job.getStatus());
        }

        @Test
        void shouldBeIdempotentWhenAlreadyActive() {
            assertDoesNotThrow(() -> job.activate("admin"));
            assertEquals(JobStatus.ACTIVE, job.getStatus());
        }

        @Test
        void shouldThrowWhenActivatingDeletedJob() {
            job.delete("admin");
            assertThrows(IllegalStateException.class, () -> job.activate("admin"));
        }

        @Test
        void shouldUpdateUpdatedByOnActivate() {
            job.pause("admin");
            job.activate("ops");
            assertEquals("ops", job.getUpdatedBy());
        }
    }

    // -------------------------------------------------------------------------
    // delete()
    // -------------------------------------------------------------------------

    @Nested
    class Delete {

        @Test
        void shouldDeleteActiveJob() {
            job.delete("admin");
            assertEquals(JobStatus.DELETED, job.getStatus());
        }

        @Test
        void shouldDeletePausedJob() {
            job.pause("admin");
            job.delete("admin");
            assertEquals(JobStatus.DELETED, job.getStatus());
        }

        @Test
        void shouldThrowWhenDeletingAlreadyDeletedJob() {
            job.delete("admin");
            assertThrows(IllegalStateException.class, () -> job.delete("admin"));
        }

        @Test
        void shouldUpdateUpdatedByOnDelete() {
            job.delete("admin");
            assertEquals("admin", job.getUpdatedBy());
        }
    }

    // -------------------------------------------------------------------------
    // Mutators
    // -------------------------------------------------------------------------

    @Nested
    class Mutators {

        @Test
        void shouldRenameJob() {
            job.rename("new-name", "admin");
            assertEquals("new-name", job.getName());
        }

        @Test
        void shouldThrowOnRenameWithNullName() {
            assertThrows(NullPointerException.class, () -> job.rename(null, "admin"));
        }

        @Test
        void shouldUpdateDescription() {
            job.updateDescription("new desc", "admin");
            assertEquals("new desc", job.getDescription());
        }

        @Test
        void shouldAllowNullDescription() {
            job.updateDescription(null, "admin");
            assertNull(job.getDescription());
        }

        @Test
        void shouldUpdateHandlerName() {
            job.updateHandlerName("newHandler", "admin");
            assertEquals("newHandler", job.getHandlerName());
        }

        @Test
        void shouldThrowOnNullHandlerName() {
            assertThrows(NullPointerException.class,
                    () -> job.updateHandlerName(null, "admin"));
        }

        @Test
        void shouldUpdatePayload() {
            job.updatePayload("{\"x\":1}", "admin");
            assertEquals("{\"x\":1}", job.getPayload());
        }

        @Test
        void shouldAllowNullPayload() {
            job.updatePayload(null, "admin");
            assertNull(job.getPayload());
        }

        @Test
        void shouldUpdateSchedule() {
            CronExpression newSchedule = new CronExpression("0 0 * * * *");
            job.updateSchedule(newSchedule, "admin");
            assertEquals(newSchedule, job.getSchedule());
        }

        @Test
        void shouldThrowOnNullSchedule() {
            assertThrows(NullPointerException.class,
                    () -> job.updateSchedule(null, "admin"));
        }

        @Test
        void shouldUpdateRetryPolicy() {
            RetryPolicy newPolicy = new RetryPolicy(5, 2000L, 2.0);
            job.updateRetryPolicy(newPolicy, "admin");
            assertEquals(newPolicy, job.getRetryPolicy());
        }

        @Test
        void shouldThrowOnNullRetryPolicy() {
            assertThrows(NullPointerException.class,
                    () -> job.updateRetryPolicy(null, "admin"));
        }
    }

    // -------------------------------------------------------------------------
    // toString()
    // -------------------------------------------------------------------------

    @Nested
    class ToStringTest {

        @Test
        void shouldContainKeyFields() {
            String result = job.toString();
            assertAll(
                    () -> assertTrue(result.contains(ID)),
                    () -> assertTrue(result.contains(NAMESPACE_ID)),
                    () -> assertTrue(result.contains(NAME)),
                    () -> assertTrue(result.contains(HANDLER))
            );
        }
    }
}
