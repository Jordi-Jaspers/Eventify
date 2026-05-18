package io.github.eventify.api.admin.model;

import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static io.github.eventify.api.admin.model.AdminAuditLogMetaData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DisplayName("Unit Test - Admin Audit Log MetaData")
public class AdminAuditLogMetaDataTest extends UnitTest {

    private AdminAuditLogMetaData metaData;

    @BeforeEach
    public void setUp() {
        metaData = new AdminAuditLogMetaData();
    }

    @Test
    @DisplayName("Should define METHOD field as sortable")
    public void shouldDefineMethodFieldAsSortable() {
        // Given: MetaData is initialized

        // When: Checking if METHOD field is sortable
        final boolean sortable = metaData.getSortableFields().contains(METHOD);

        // Then: METHOD should be sortable
        assertThat(sortable, is(true));
    }

    @Test
    @DisplayName("Should define PATH field as sortable")
    public void shouldDefinePathFieldAsSortable() {
        // Given: MetaData is initialized

        // When: Checking if PATH field is sortable
        final boolean sortable = metaData.getSortableFields().contains(PATH);

        // Then: PATH should be sortable
        assertThat(sortable, is(true));
    }

    @Test
    @DisplayName("Should define CREATED_AT field as sortable")
    public void shouldDefineCreatedAtFieldAsSortable() {
        // Given: MetaData is initialized

        // When: Checking if CREATED_AT field is sortable
        final boolean sortable = metaData.getSortableFields().contains(CREATED_AT);

        // Then: CREATED_AT should be sortable
        assertThat(sortable, is(true));
    }

    @Test
    @DisplayName("Should define STATUS_CODE field")
    public void shouldDefineStatusCodeField() {
        // Given: MetaData is initialized

        // When: Checking if STATUS_CODE field is registered
        final boolean known = metaData.getColumnNames().containsKey(STATUS_CODE);

        // Then: STATUS_CODE should be known
        assertThat(known, is(true));
    }

    @Test
    @DisplayName("Should define ACTOR field for actor name search")
    public void shouldDefineActorField() {
        // Given: MetaData is initialized

        // When: Checking if ACTOR field is registered
        final boolean known = metaData.getColumnNames().containsKey(ACTOR);

        // Then: ACTOR should be known
        assertThat(known, is(true));
    }

    @Test
    @DisplayName("Should build specification for 2xx status range")
    public void shouldBuildSpecificationFor2xxStatusRange() {
        // Given: A search input with 2xx status filter
        final SortablePageInput input = new SortablePageInput();
        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName(STATUS);
        statusFilter.setTextValueList(List.of("2xx"));
        input.getSearchInputs().add(statusFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should build specification for 4xx status range")
    public void shouldBuildSpecificationFor4xxStatusRange() {
        // Given: A search input with 4xx status filter
        final SortablePageInput input = new SortablePageInput();
        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName(STATUS);
        statusFilter.setTextValueList(List.of("4xx"));
        input.getSearchInputs().add(statusFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should build specification for 5xx status range")
    public void shouldBuildSpecificationFor5xxStatusRange() {
        // Given: A search input with 5xx status filter
        final SortablePageInput input = new SortablePageInput();
        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName(STATUS);
        statusFilter.setTextValueList(List.of("5xx"));
        input.getSearchInputs().add(statusFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should build specification for multiple status ranges")
    public void shouldBuildSpecificationForMultipleStatusRanges() {
        // Given: A search input with multiple status filters
        final SortablePageInput input = new SortablePageInput();
        final SearchInput statusFilter = new SearchInput();
        statusFilter.setFieldName(STATUS);
        statusFilter.setTextValueList(List.of("2xx", "4xx"));
        input.getSearchInputs().add(statusFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should build specification without status filter when none provided")
    public void shouldBuildSpecificationWithoutStatusFilter() {
        // Given: A search input with no status filter
        final SortablePageInput input = new SortablePageInput();

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null (no-op conjunction)
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should define EXCLUDE_PATH field as not sortable")
    public void shouldDefineExcludePathFieldAsNotSortable() {
        // Given: MetaData is initialized

        // When: Checking if EXCLUDE_PATH field is registered and not sortable
        final boolean known = metaData.getColumnNames().containsKey(EXCLUDE_PATH);
        final boolean sortable = metaData.getSortableFields().contains(EXCLUDE_PATH);

        // Then: EXCLUDE_PATH should be known but not sortable
        assertThat(known, is(true));
        assertThat(sortable, is(false));
    }

    @Test
    @DisplayName("Should build specification with excludePath filter")
    public void shouldBuildSpecificationWithExcludePathFilter() {
        // Given: A search input with excludePath filter
        final SortablePageInput input = new SortablePageInput();
        final SearchInput excludeFilter = new SearchInput();
        excludeFilter.setFieldName(EXCLUDE_PATH);
        excludeFilter.setTextValue("/actuator");
        input.getSearchInputs().add(excludeFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }

    @Test
    @DisplayName("Should build specification with multiple comma-separated excludePath values")
    public void shouldBuildSpecificationWithMultipleExcludePaths() {
        // Given: A search input with multiple comma-separated excludePath values
        final SortablePageInput input = new SortablePageInput();
        final SearchInput excludeFilter = new SearchInput();
        excludeFilter.setFieldName(EXCLUDE_PATH);
        excludeFilter.setTextValue("/actuator,/health");
        input.getSearchInputs().add(excludeFilter);

        // When: Building specification
        final Specification<?> spec = metaData.toSearchSpecification(input);

        // Then: Specification should be non-null
        assertThat(spec, is(notNullValue()));
    }
}
