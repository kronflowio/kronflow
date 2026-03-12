package io.github.kronflow.core.service;

import io.github.kronflow.core.exception.DuplicateNameException;
import io.github.kronflow.core.model.Namespace;
import io.github.kronflow.core.spi.IdGenerator;
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
class NamespaceServiceTest {

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private static final String NS_ID = "ns-1";
    private static final String NAME = "my-namespace";
    private static final String DESCRIPTION = "A test namespace";
    private static final String CREATED_BY = "user-1";
    private static final String UPDATED_BY = "user-2";

    @Mock
    private NamespaceStore namespaceStore;
    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private NamespaceService namespaceService;

    private Namespace existingNamespace;

    @BeforeEach
    void setUp() {
        existingNamespace = new Namespace(NS_ID, NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);
    }

    // -------------------------------------------------------------------------
    // createNamespace()
    // -------------------------------------------------------------------------

    @Nested
    class CreateNamespace {

        @Test
        void shouldCreateAndPersistNamespace() {
            when(namespaceStore.existsNamespaceByName(NAME)).thenReturn(false);
            when(idGenerator.generateId()).thenReturn(NS_ID);

            Namespace result = namespaceService.createNamespace(NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);

            assertAll(
                    () -> assertEquals(NS_ID, result.getId()),
                    () -> assertEquals(NAME, result.getName()),
                    () -> assertEquals(DESCRIPTION, result.getDescription()),
                    () -> assertEquals(CREATED_BY, result.getCreatedBy()),
                    () -> assertTrue(result.isActive())
            );

            ArgumentCaptor<Namespace> captor = ArgumentCaptor.forClass(Namespace.class);
            verify(namespaceStore).create(captor.capture());
            assertEquals(NS_ID, captor.getValue().getId());
        }

        @Test
        void shouldThrowDuplicateNameExceptionWhenNameAlreadyExists() {
            when(namespaceStore.existsNamespaceByName(NAME)).thenReturn(true);

            DuplicateNameException ex = assertThrows(DuplicateNameException.class,
                    () -> namespaceService.createNamespace(NAME, DESCRIPTION, CREATED_BY, UPDATED_BY));

            assertTrue(ex.getMessage().contains(NAME));
            verify(namespaceStore, never()).create(any());
        }

        @Test
        void shouldUseIdFromIdGenerator() {
            when(namespaceStore.existsNamespaceByName(NAME)).thenReturn(false);
            when(idGenerator.generateId()).thenReturn("generated-id-99");

            Namespace result = namespaceService.createNamespace(NAME, DESCRIPTION, CREATED_BY, UPDATED_BY);

            assertEquals("generated-id-99", result.getId());
        }

        @Test
        void shouldAllowNullDescription() {
            when(namespaceStore.existsNamespaceByName(NAME)).thenReturn(false);
            when(idGenerator.generateId()).thenReturn(NS_ID);

            Namespace result = namespaceService.createNamespace(NAME, null, CREATED_BY, UPDATED_BY);

            assertNull(result.getDescription());
            verify(namespaceStore).create(any());
        }

        @Test
        void shouldNeverCallIdGeneratorWhenDuplicateNameDetected() {
            when(namespaceStore.existsNamespaceByName(NAME)).thenReturn(true);

            assertThrows(DuplicateNameException.class,
                    () -> namespaceService.createNamespace(NAME, DESCRIPTION, CREATED_BY, UPDATED_BY));

            verify(idGenerator, never()).generateId();
        }
    }

    // -------------------------------------------------------------------------
    // updateNameSpace()
    // -------------------------------------------------------------------------

    @Nested
    class UpdateNamespace {

        @Test
        void shouldUpdateAllFieldsAndPersist() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));
            when(namespaceStore.existsNamespaceByName("new-name")).thenReturn(false);

            Namespace result = namespaceService.updateNameSpace(NS_ID, "new-name", "new-desc", UPDATED_BY);

            assertAll(
                    () -> assertEquals("new-name", result.getName()),
                    () -> assertEquals("new-desc", result.getDescription()),
                    () -> assertEquals(UPDATED_BY, result.getUpdatedBy())
            );
            verify(namespaceStore).update(NS_ID, existingNamespace);
        }

        @Test
        void shouldThrowWhenNamespaceDoesNotExist() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.empty());

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> namespaceService.updateNameSpace(NS_ID, "new-name", "new-desc", UPDATED_BY));

            assertTrue(ex.getMessage().contains(NS_ID));
            verify(namespaceStore, never()).update(any(), any());
        }

        @Test
        void shouldThrowDuplicateNameExceptionWhenNewNameTakenByAnotherNamespace() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));
            when(namespaceStore.existsNamespaceByName("taken-name")).thenReturn(true);

            DuplicateNameException ex = assertThrows(DuplicateNameException.class,
                    () -> namespaceService.updateNameSpace(NS_ID, "taken-name", DESCRIPTION, UPDATED_BY));

            assertTrue(ex.getMessage().contains(NS_ID));
            verify(namespaceStore, never()).update(any(), any());
        }

        @Test
        void shouldAllowUpdateWithSameNameWithoutDuplicateCheck() {
            // same name → existsNamespaceByName must NOT be called
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));

            assertDoesNotThrow(() ->
                    namespaceService.updateNameSpace(NS_ID, NAME, "updated-desc", UPDATED_BY));

            verify(namespaceStore, never()).existsNamespaceByName(NAME);
            verify(namespaceStore).update(NS_ID, existingNamespace);
        }

        @Test
        void shouldAllowNullDescriptionOnUpdate() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));
            when(namespaceStore.existsNamespaceByName("new-name")).thenReturn(false);

            Namespace result = namespaceService.updateNameSpace(NS_ID, "new-name", null, UPDATED_BY);

            assertNull(result.getDescription());
        }
    }

    // -------------------------------------------------------------------------
    // getNamespaceById()
    // -------------------------------------------------------------------------

    @Nested
    class GetNamespaceById {

        @Test
        void shouldReturnNamespaceWhenFound() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));

            Optional<Namespace> result = namespaceService.getNamespaceById(NS_ID);

            assertTrue(result.isPresent());
            assertEquals(NS_ID, result.get().getId());
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.empty());

            Optional<Namespace> result = namespaceService.getNamespaceById(NS_ID);

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldDelegateToNamespaceStore() {
            when(namespaceStore.findById(NS_ID)).thenReturn(Optional.of(existingNamespace));

            namespaceService.getNamespaceById(NS_ID);

            verify(namespaceStore).findById(NS_ID);
        }
    }

    // -------------------------------------------------------------------------
    // getAllNamespaces()
    // -------------------------------------------------------------------------

    @Nested
    class GetAllNamespaces {

        @Test
        void shouldReturnAllNamespaces() {
            Namespace ns2 = new Namespace("ns-2", "other-ns", "desc", CREATED_BY, null);
            when(namespaceStore.findAll()).thenReturn(List.of(existingNamespace, ns2));

            List<Namespace> result = namespaceService.getAllNamespaces();

            assertEquals(2, result.size());
            verify(namespaceStore).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoNamespacesExist() {
            when(namespaceStore.findAll()).thenReturn(List.of());

            List<Namespace> result = namespaceService.getAllNamespaces();

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldDelegateToNamespaceStore() {
            when(namespaceStore.findAll()).thenReturn(List.of());

            namespaceService.getAllNamespaces();

            verify(namespaceStore).findAll();
        }
    }

    // -------------------------------------------------------------------------
    // deleteNamespace()
    // -------------------------------------------------------------------------

    @Nested
    class DeleteNamespace {

        @Test
        void shouldDeleteWhenNamespaceExists() {
            when(namespaceStore.existsById(NS_ID)).thenReturn(true);

            assertDoesNotThrow(() -> namespaceService.deleteNamespace(NS_ID));

            verify(namespaceStore).deleteById(NS_ID);
        }

        @Test
        void shouldThrowWhenNamespaceDoesNotExist() {
            when(namespaceStore.existsById(NS_ID)).thenReturn(false);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> namespaceService.deleteNamespace(NS_ID));

            assertTrue(ex.getMessage().contains(NS_ID));
        }

        @Test
        void shouldNeverCallDeleteByIdWhenNamespaceMissing() {
            when(namespaceStore.existsById(NS_ID)).thenReturn(false);

            assertThrows(IllegalArgumentException.class,
                    () -> namespaceService.deleteNamespace(NS_ID));

            verify(namespaceStore, never()).deleteById(any());
        }
    }
}
