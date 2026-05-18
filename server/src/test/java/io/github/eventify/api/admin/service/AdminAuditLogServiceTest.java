package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.AdminAuditLogMetaData;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.common.audit.repository.AuditLogRepository;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Admin Audit Log Service")
public class AdminAuditLogServiceTest extends UnitTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AdminAuditLogMetaData adminAuditLogMetaData;

    private AdminAuditLogService adminAuditLogService;

    @BeforeEach
    public void setUp() {
        adminAuditLogService = new AdminAuditLogService(
            auditLogRepository,
            adminAuditLogMetaData
        );
        when(adminAuditLogMetaData.toSearchSpecification(any()))
            .thenReturn((root, query, cb) -> cb.conjunction());
    }

    @Test
    @DisplayName("Should call repository findAll with spec and pageable when searching")
    public void shouldCallRepositoryFindAllWithSpecAndPageable() {
        // Given: A search input
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // And: MetaData returns a sort
        when(adminAuditLogMetaData.toSort(any())).thenReturn(org.springframework.data.domain.Sort.unsorted());

        // And: Repository returns empty page
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // When: Searching audit log
        final Page<AuditLog> result = adminAuditLogService.searchAuditLog(input);

        // Then: Repository should be called with spec and pageable
        verify(auditLogRepository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(result, is(notNullValue()));
    }

    @Test
    @DisplayName("Should call metaData toSort when building pageable")
    public void shouldCallMetaDataToSortWhenBuildingPageable() {
        // Given: A search input with sort order
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // And: MetaData returns unsorted
        when(adminAuditLogMetaData.toSort(any())).thenReturn(org.springframework.data.domain.Sort.unsorted());
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // When: Searching audit log
        adminAuditLogService.searchAuditLog(input);

        // Then: MetaData toSort should be called with the sort order from input
        verify(adminAuditLogMetaData).toSort(input.getSortOrder());
    }

    @Test
    @DisplayName("Should return page from repository")
    public void shouldReturnPageFromRepository() {
        // Given: A search input
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // And: Repository returns a page with entries
        final AuditLog auditLog = anAuditLog(1L, 100L, "GET", "/api/v1/test", (short) 200);
        final Page<AuditLog> expectedPage = new PageImpl<>(List.of(auditLog));

        when(adminAuditLogMetaData.toSort(any())).thenReturn(org.springframework.data.domain.Sort.unsorted());
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(expectedPage);

        // When: Searching audit log
        final Page<AuditLog> result = adminAuditLogService.searchAuditLog(input);

        // Then: Result should match repository output
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent(), hasSize(1));
        assertThat(result.getContent().get(0).getId(), is(equalTo(1L)));
    }

    @Test
    @DisplayName("Should return empty page when repository returns no results")
    public void shouldReturnEmptyPageWhenNoResults() {
        // Given: A search input
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        // And: Repository returns empty page
        when(adminAuditLogMetaData.toSort(any())).thenReturn(org.springframework.data.domain.Sort.unsorted());
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When: Searching audit log
        final Page<AuditLog> result = adminAuditLogService.searchAuditLog(input);

        // Then: Result should be empty
        assertThat(result.getTotalElements(), is(0L));
        assertThat(result.getContent(), is(empty()));
    }

    @Test
    @DisplayName("Should build PageRequest with correct page number and size")
    public void shouldBuildPageRequestWithCorrectPageNumberAndSize() {
        // Given: A search input with specific page settings
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(2);
        input.setPageSize(15);

        when(adminAuditLogMetaData.toSort(any())).thenReturn(org.springframework.data.domain.Sort.unsorted());
        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(Page.empty());

        // When: Searching audit log
        adminAuditLogService.searchAuditLog(input);

        // Then: Repository should be called with pageable containing correct page/size
        final org.mockito.ArgumentCaptor<Pageable> pageableCaptor =
            org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findAll(any(Specification.class), pageableCaptor.capture());

        final Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber(), is(2));
        assertThat(pageable.getPageSize(), is(15));
    }

    // ========================= HELPER METHODS =========================

    private static AuditLog anAuditLog(final Long id, final Long actorId, final String method,
        final String path, final short statusCode) {
        final User actor = new User();
        actor.setId(actorId);
        final AuditLog log = new AuditLog(
            actor,
            method,
            path,
            statusCode,
            null,
            "127.0.0.1",
            java.time.OffsetDateTime.now()
        );
        log.setId(id);
        return log;
    }
}
