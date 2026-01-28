package io.github.eventify.api.watchlist.model.validator;

import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.watchlist.model.request.ChannelGroupRequest;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistFiltersRequest;
import io.github.eventify.support.UnitTest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.eventify.api.watchlist.model.validator.WatchlistValidator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit Test - Watchlist Validator.
 */
@DisplayName("Unit Test - Watchlist Validator")
public class WatchlistValidatorTest extends UnitTest {

    private WatchlistValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new WatchlistValidator();
    }

    // ==================== Create Request Tests ====================

    @Test
    @DisplayName("Should accept valid create request")
    public void shouldAcceptValidCreateRequest() {
        // Given: Valid create request
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setDescription("Production errors");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept create request with configuration")
    public void shouldAcceptCreateRequestWithConfiguration() {
        // Given: Valid request with configuration
        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(1L, 2L, 3L));

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setConfiguration(config);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept create request with filters")
    public void shouldAcceptCreateRequestWithFilters() {
        // Given: Valid request with filters
        final WatchlistFiltersRequest filters = new WatchlistFiltersRequest()
            .setTimeRange(TimeRange.LAST_24H)
            .setOnlyCritical(false)
            .setSortBySeverity(true);

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setFilters(filters);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept create request with time range 7d")
    public void shouldAcceptCreateRequestWithTimeRange7d() {
        // Given: Valid request with time range 7d
        final WatchlistFiltersRequest filters = new WatchlistFiltersRequest()
            .setTimeRange(TimeRange.LAST_7D);

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setFilters(filters);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept create request with time range 30d")
    public void shouldAcceptCreateRequestWithTimeRange30d() {
        // Given: Valid request with time range 30d
        final WatchlistFiltersRequest filters = new WatchlistFiltersRequest()
            .setTimeRange(TimeRange.LAST_30D);

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("My Watchlist")
            .setFilters(filters);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null create request body")
    public void shouldRejectNullCreateRequestBody() {
        // Given: Null request
        final CreateWatchlistRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(1));
        assertThat(
            exception.getValidationResult().getErrors().get(0).getCode(),
            is(equalTo(BODY_IS_MISSING))
        );
    }

    @Test
    @DisplayName("Should reject null name in create request")
    public void shouldRejectNullNameInCreateRequest() {
        // Given: Request with null name
        final CreateWatchlistRequest request = new CreateWatchlistRequest();
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty name in create request")
    public void shouldRejectEmptyNameInCreateRequest() {
        // Given: Request with empty name
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank name in create request")
    public void shouldRejectBlankNameInCreateRequest() {
        // Given: Request with blank name
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("   ");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name exceeding 100 characters in create request")
    public void shouldRejectNameExceeding100CharactersInCreateRequest() {
        // Given: Request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName(longName);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept name with exactly 100 characters in create request")
    public void shouldAcceptNameWithExactly100CharactersInCreateRequest() {
        // Given: Request with name of exactly 100 characters
        final String maxName = "a".repeat(100);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName(maxName);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject description exceeding 500 characters in create request")
    public void shouldRejectDescriptionExceeding500CharactersInCreateRequest() {
        // Given: Request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(longDescription);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(DESCRIPTION) &&
                        error.getCode().equals(DESCRIPTION_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept description with exactly 500 characters in create request")
    public void shouldAcceptDescriptionWithExactly500CharactersInCreateRequest() {
        // Given: Request with description of exactly 500 characters
        final String maxDescription = "a".repeat(500);
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(maxDescription);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null value in configuration channel IDs list")
    public void shouldRejectNullValueInConfigurationChannelIdsList() {
        // Given: Request with null in configuration channel IDs
        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(Arrays.asList(1L, null, 3L));

        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Valid Name")
            .setConfiguration(config);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(CONFIGURATION_CHANNEL_IDS) &&
                        error.getCode().equals(CHANNEL_ID_REQUIRED)
                ),
            is(true)
        );
    }

    // ==================== Update Request Tests ====================

    @Test
    @DisplayName("Should accept valid update request")
    public void shouldAcceptValidUpdateRequest() {
        // Given: Valid update request
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Updated Watchlist")
            .setDescription("Updated description");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept update request without description")
    public void shouldAcceptUpdateRequestWithoutDescription() {
        // Given: Valid update request without description
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Updated Watchlist");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null update request body")
    public void shouldRejectNullUpdateRequestBody() {
        // Given: Null update request
        final UpdateWatchlistRequest request = null;
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().getErrors().size(), is(1));
        assertThat(
            exception.getValidationResult().getErrors().get(0).getCode(),
            is(equalTo(BODY_IS_MISSING))
        );
    }

    @Test
    @DisplayName("Should reject null name in update request")
    public void shouldRejectNullNameInUpdateRequest() {
        // Given: Update request with null name
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest();
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject empty name in update request")
    public void shouldRejectEmptyNameInUpdateRequest() {
        // Given: Update request with empty name
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject blank name in update request")
    public void shouldRejectBlankNameInUpdateRequest() {
        // Given: Update request with blank name
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("   ");
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject name exceeding 100 characters in update request")
    public void shouldRejectNameExceeding100CharactersInUpdateRequest() {
        // Given: Update request with name exceeding 100 characters
        final String longName = "a".repeat(101);
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName(longName);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(NAME) &&
                        error.getCode().equals(NAME_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept name with exactly 100 characters in update request")
    public void shouldAcceptNameWithExactly100CharactersInUpdateRequest() {
        // Given: Update request with name of exactly 100 characters
        final String maxName = "a".repeat(100);
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName(maxName);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject description exceeding 500 characters in update request")
    public void shouldRejectDescriptionExceeding500CharactersInUpdateRequest() {
        // Given: Update request with description exceeding 500 characters
        final String longDescription = "a".repeat(501);
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(longDescription);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(DESCRIPTION) &&
                        error.getCode().equals(DESCRIPTION_TOO_LONG)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept description with exactly 500 characters in update request")
    public void shouldAcceptDescriptionWithExactly500CharactersInUpdateRequest() {
        // Given: Update request with description of exactly 500 characters
        final String maxDescription = "a".repeat(500);
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(maxDescription);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should reject null value in configuration channel IDs list in update request")
    public void shouldRejectNullValueInConfigurationChannelIdsListInUpdateRequest() {
        // Given: Update request with null in configuration channel IDs
        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(Arrays.asList(1L, null, 3L));

        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Valid Name")
            .setConfiguration(config);
        final ValidationResult result = new ValidationResult();

        // When & Then: Should throw ValidationException
        final ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.validate(request, result)
        );

        assertThat(exception.getValidationResult().hasErrors(), is(true));
        assertThat(
            exception.getValidationResult().getErrors().stream()
                .anyMatch(
                    error -> error.getField().equals(CONFIGURATION_CHANNEL_IDS) &&
                        error.getCode().equals(CHANNEL_ID_REQUIRED)
                ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept update request with null description")
    public void shouldAcceptUpdateRequestWithNullDescription() {
        // Given: Update request with null description
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Valid Name")
            .setDescription(null);
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    @Test
    @DisplayName("Should accept update request with empty description")
    public void shouldAcceptUpdateRequestWithEmptyDescription() {
        // Given: Update request with empty description
        final UpdateWatchlistRequest request = new UpdateWatchlistRequest()
            .setName("Valid Name")
            .setDescription("");
        final ValidationResult result = new ValidationResult();

        // When: Validating request
        validator.validate(request, result);

        // Then: Validation should pass
        assertThat(result.hasErrors(), is(false));
    }

    // ==================== Duplicate Channel Validation Tests ====================

    @Test
    @DisplayName("Should reject duplicate channel ID in standalone list")
    void shouldRejectDuplicateChannelIdInStandaloneList() {
        // Given: Request with duplicate channel ID in channelIds
        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(1L, 2L, 1L));  // 1L appears twice
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Test")
            .setConfiguration(config);
        final ValidationResult validationResult = new ValidationResult();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> validator.validate(request, validationResult))
            .isInstanceOf(ValidationException.class);

        assertThat(
            validationResult.getErrors().stream()
                .anyMatch(e -> e.getCode().contains("Channel ID 1 appears multiple times")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject same channel ID in multiple groups")
    void shouldRejectSameChannelIdInMultipleGroups() {
        // Given: Request with same channel in two groups
        final ChannelGroupRequest group1 = new ChannelGroupRequest()
            .setName("Group 1")
            .setChannelIds(List.of(1L, 2L));
        final ChannelGroupRequest group2 = new ChannelGroupRequest()
            .setName("Group 2")
            .setChannelIds(List.of(3L, 1L));  // 1L also in group1

        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setGroups(List.of(group1, group2));
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Test")
            .setConfiguration(config);
        final ValidationResult validationResult = new ValidationResult();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> validator.validate(request, validationResult))
            .isInstanceOf(ValidationException.class);

        assertThat(
            validationResult.getErrors().stream()
                .anyMatch(e -> e.getCode().contains("Channel ID 1 appears multiple times")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should reject channel ID that is both standalone and in group")
    void shouldRejectChannelIdBothStandaloneAndInGroup() {
        // Given: Request with channel both standalone and in group
        final ChannelGroupRequest group = new ChannelGroupRequest()
            .setName("Group 1")
            .setChannelIds(List.of(1L, 2L));

        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(1L))  // 1L also in group
            .setGroups(List.of(group));
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Test")
            .setConfiguration(config);
        final ValidationResult validationResult = new ValidationResult();

        // When/Then: Validation should fail
        assertThatThrownBy(() -> validator.validate(request, validationResult))
            .isInstanceOf(ValidationException.class);

        assertThat(
            validationResult.getErrors().stream()
                .anyMatch(e -> e.getCode().contains("Channel ID 1 appears multiple times")),
            is(true)
        );
    }

    @Test
    @DisplayName("Should accept configuration with unique channel IDs")
    void shouldAcceptConfigurationWithUniqueChannelIds() {
        // Given: Request with unique channel IDs across standalone and groups
        final ChannelGroupRequest group1 = new ChannelGroupRequest()
            .setName("Group 1")
            .setChannelIds(List.of(3L, 4L));
        final ChannelGroupRequest group2 = new ChannelGroupRequest()
            .setName("Group 2")
            .setChannelIds(List.of(5L, 6L));

        final WatchlistConfigurationRequest config = new WatchlistConfigurationRequest()
            .setChannelIds(List.of(1L, 2L))
            .setGroups(List.of(group1, group2));
        final CreateWatchlistRequest request = new CreateWatchlistRequest()
            .setName("Test")
            .setConfiguration(config);
        final ValidationResult validationResult = new ValidationResult();

        // When/Then: Validation should pass
        assertThatCode(() -> validator.validate(request, validationResult))
            .doesNotThrowAnyException();
    }
}
