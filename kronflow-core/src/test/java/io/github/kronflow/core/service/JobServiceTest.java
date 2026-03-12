package io.github.kronflow.core.service;

import io.github.kronflow.core.model.JobDefinition;
import io.github.kronflow.core.model.Namespace;
import io.github.kronflow.core.model.enums.JobStatus;
import io.github.kronflow.core.model.value.CronExpression;
import io.github.kronflow.core.model.value.RetryPolicy;
import io.github.kronflow.core.spi.IdGenerator;
import io.github.kronflow.core.spi.JobStore;
import io.github.kronflow.core.spi.NamespaceStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final String JOB_ID = "job-1";
    private static final String NAMESPACE_ID = "ns-1";
    private static final String NAME = "my-job";
    private static final String DESCRIPTION = "A test job";
    private static final String HANDLER = "myHandler";
    private static final String PAYLOAD = "{\"key\":\"value\"}";
    private static final String CREATED_BY = "user-1";
    private static final String UPDATED_BY = "user-2";

    @Mock
    private JobStore jobStore;
    @Mock
    private NamespaceStore namespaceStore;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private JobService jobService;

    private CronExpression schedule;
    private RetryPolicy retryPolicy;
    private Namespace namespace;
    private JobDefinition existingJob;

    @BeforeEach
    void setUp() {
        schedule = new CronExpression("0 * * * * *");
        retryPolicy = new RetryPolicy(3, 1000L, 2.0);
        namespace = new Namespace(NAMESPACE_ID, "my-ns", "desc", CREATED_BY, null);
        existingJob = new JobDefinition(
                JOB_ID, NAMESPACE_ID, CREATED_BY,
                NAME, DESCRIPTION, HANDLER,
                schedule, PAYLOAD, retryPolicy
        );
    }

    // -------------------------------------------------------------------------
    // createJob()
    // -------------------------------------------------------------------------

    @Nested
    class CreateJob {

        @Test
        void shouldCreateAndPersistJob() {
            when(namespaceStore.findById(NAMESPACE_ID)).thenReturn(Optional.of(namespace));
            when(jobStore.findByNamespaceAndName(NAMESPACE_ID, NAME)).thenReturn(Optional.empty());
            when(idGenerator.generateId()).thenReturn(JOB_ID);

            JobDefinition result = jobService.createJob(
                    NAMESPACE_ID, NAME, DESCRIPTION, HANDLER,
                    PAYLOAD, schedule, retryPolicy, CREATED_BY
            );

            assertAll(
                    () -> assertEquals(JOB_ID, result.getId()),
                    () -> assertEquals(NAMESPACE_ID, result.getNamespaceId()),
                    () -> assertEquals(NAME, result.getName()),
                    () -> assertEquals(DESCRIPTION, result.getDescription()),
                    () -> assertEquals(HANDLER, result.getHandlerName()),
                    () -> assertEquals(PAYLOAD, result.getPayload()),
                    () -> assertEquals(schedule, result.getSchedule()),
                    () -> assertEquals(retryPolicy, result.getRetryPolicy()),
                    () -> assertEquals(JobStatus.ACTIVE, result.getStatus())
            );

            ArgumentCaptor<JobDefinition> captor = ArgumentCaptor.forClass(JobDefinition.class);
            verify(jobStore).create(captor.capture());
            assertEquals(JOB_ID, captor.getValue().getId());
        }

        @Test
        void shouldThrowWhenNamespaceDoesNotExist() {
            when(namespaceStore.findById(NAMESPACE_ID)).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jobService.createJob(
                            NAMESPACE_ID, NAME, DESCRIPTION, HANDLER,
                            PAYLOAD, schedule, retryPolicy, CREATED_BY));

            assertTrue(ex.getMessage().contains(NAMESPACE_ID));
            verify(jobStore, never()).create(any());
        }

        @Test
        void shouldThrowWhenJobWithSameNameAlreadyExists() {
            when(namespaceStore.findById(NAMESPACE_ID)).thenReturn(Optional.of(namespace));
            when(jobStore.findByNamespaceAndName(NAMESPACE_ID, NAME))
                    .thenReturn(Optional.of(existingJob));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jobService.createJob(
                            NAMESPACE_ID, NAME, DESCRIPTION, HANDLER,
                            PAYLOAD, schedule, retryPolicy, CREATED_BY));

            assertTrue(ex.getMessage().contains(NAME));
            verify(jobStore, never()).create(any());
        }

        @Test
        void shouldUseIdFromIdGenerator() {
            when(namespaceStore.findById(NAMESPACE_ID)).thenReturn(Optional.of(namespace));
            when(jobStore.findByNamespaceAndName(NAMESPACE_ID, NAME)).thenReturn(Optional.empty());
            when(idGenerator.generateId()).thenReturn("generated-id-42");

            JobDefinition result = jobService.createJob(
                    NAMESPACE_ID, NAME, DESCRIPTION, HANDLER,
                    PAYLOAD, schedule, retryPolicy, CREATED_BY);

            assertEquals("generated-id-42", result.getId());
        }

        @Test
        void shouldUseDefaultRetryPolicyWhenNullPassed() {
            when(namespaceStore.findById(NAMESPACE_ID)).thenReturn(Optional.of(namespace));
            when(jobStore.findByNamespaceAndName(NAMESPACE_ID, NAME)).thenReturn(Optional.empty());
            when(idGenerator.generateId()).thenReturn(JOB_ID);

            JobDefinition result = jobService.createJob(
                    NAMESPACE_ID, NAME, DESCRIPTION, HANDLER,
                    PAYLOAD, schedule, null, CREATED_BY);

            assertEquals(RetryPolicy.DEFAULT, result.getRetryPolicy());
        }
    }

    // -------------------------------------------------------------------------
    // updateJob()
    // -------------------------------------------------------------------------

    @Nested
    class UpdateJob {

        private JobDefinition buildRequest(JobStatus status) {
            JobDefinition req = new JobDefinition(
                    JOB_ID, NAMESPACE_ID, UPDATED_BY,
                    "updated-name", "updated-desc", "updatedHandler",
                    new CronExpression("0 0 * * * *"), "{\"x\":1}",
                    new RetryPolicy(5, 2000L, 2)
            );
            // drive status via lifecycle methods
            if (status == JobStatus.PAUSED) req.pause(UPDATED_BY);
            if (status == JobStatus.DELETED) req.delete(UPDATED_BY);
            return req;
        }

        @Test
        void shouldUpdateAllFieldsAndPersist() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));
            JobDefinition req = buildRequest(JobStatus.ACTIVE);

            JobDefinition result = jobService.updateJob(JOB_ID, req, UPDATED_BY);

            assertAll(
                    () -> assertEquals("updated-name", result.getName()),
                    () -> assertEquals("updated-desc", result.getDescription()),
                    () -> assertEquals("updatedHandler", result.getHandlerName()),
                    () -> assertEquals("{\"x\":1}", result.getPayload()),
                    () -> assertEquals(JobStatus.ACTIVE, result.getStatus())
            );
            verify(jobStore).update(existingJob);
        }

        @Test
        void shouldPauseJobWhenRequestStatusIsPaused() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));
            JobDefinition req = buildRequest(JobStatus.PAUSED);

            JobDefinition result = jobService.updateJob(JOB_ID, req, UPDATED_BY);

            assertEquals(JobStatus.PAUSED, result.getStatus());
            verify(jobStore).update(existingJob);
        }

        @Test
        void shouldDeleteJobWhenRequestStatusIsDeleted() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));
            JobDefinition req = buildRequest(JobStatus.DELETED);

            JobDefinition result = jobService.updateJob(JOB_ID, req, UPDATED_BY);

            assertEquals(JobStatus.DELETED, result.getStatus());
            verify(jobStore).update(existingJob);
        }

        @Test
        void shouldThrowWhenJobDoesNotExist() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.empty());
            JobDefinition req = buildRequest(JobStatus.ACTIVE);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jobService.updateJob(JOB_ID, req, UPDATED_BY));

            assertTrue(ex.getMessage().contains(JOB_ID));
            verify(jobStore, never()).update(any());
        }

        @Test
        void shouldThrowWhenJobIsAlreadyDeleted() {
            existingJob.delete(UPDATED_BY);
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));
            JobDefinition req = buildRequest(JobStatus.ACTIVE);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jobService.updateJob(JOB_ID, req, UPDATED_BY));

            assertTrue(ex.getMessage().contains(JOB_ID));
            verify(jobStore, never()).update(any());
        }
    }

    // -------------------------------------------------------------------------
    // getJobById()
    // -------------------------------------------------------------------------

    @Nested
    class GetJobById {

        @Test
        void shouldReturnJobWhenFound() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));

            Optional<JobDefinition> result = jobService.getJobById(JOB_ID);

            assertTrue(result.isPresent());
            assertEquals(JOB_ID, result.get().getId());
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.empty());

            Optional<JobDefinition> result = jobService.getJobById(JOB_ID);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldDelegateToJobStore() {
            when(jobStore.findById(JOB_ID)).thenReturn(Optional.of(existingJob));
            jobService.getJobById(JOB_ID);
            verify(jobStore).findById(JOB_ID);
        }
    }

    // -------------------------------------------------------------------------
    // getAllJobs()
    // -------------------------------------------------------------------------

    @Nested
    class GetAllJobs {

        @Test
        void shouldReturnAllJobsForNamespace() {
            JobDefinition job2 = new JobDefinition(
                    "job-2", NAMESPACE_ID, CREATED_BY,
                    "other-job", DESCRIPTION, HANDLER,
                    schedule, PAYLOAD, retryPolicy
            );
            when(jobStore.findAllByNamespace(NAMESPACE_ID))
                    .thenReturn(List.of(existingJob, job2));

            List<JobDefinition> result = jobService.getAllJobs(NAMESPACE_ID);

            assertEquals(2, result.size());
            verify(jobStore).findAllByNamespace(NAMESPACE_ID);
        }

        @Test
        void shouldReturnEmptyListWhenNoJobsExist() {
            when(jobStore.findAllByNamespace(NAMESPACE_ID)).thenReturn(List.of());

            List<JobDefinition> result = jobService.getAllJobs(NAMESPACE_ID);

            assertTrue(result.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // deleteJob()
    // -------------------------------------------------------------------------

    @Nested
    class DeleteJob {

        @Test
        void shouldDeleteJobWhenExists() {
            when(jobStore.existsById(JOB_ID)).thenReturn(true);

            assertDoesNotThrow(() -> jobService.deleteJob(JOB_ID));

            verify(jobStore).deleteById(JOB_ID);
        }

        @Test
        void shouldThrowWhenJobDoesNotExist() {
            when(jobStore.existsById(JOB_ID)).thenReturn(false);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> jobService.deleteJob(JOB_ID));

            assertTrue(ex.getMessage().contains(JOB_ID));
            verify(jobStore, never()).deleteById(any());
        }

        @Test
        void shouldNotCallDeleteWhenJobMissing() {
            when(jobStore.existsById(JOB_ID)).thenReturn(false);

            assertThrows(IllegalArgumentException.class, () -> jobService.deleteJob(JOB_ID));

            verify(jobStore, never()).deleteById(any());
        }
    }
}
