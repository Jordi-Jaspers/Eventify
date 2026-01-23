package io.github.eventify.api.watchlist.model.validator;

import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistFiltersRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.Set;

import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * Validator for watchlist requests.
 */
@Component
public class WatchlistValidator implements Validator<Object> {

    // Error messages
    public static final String BODY_IS_MISSING = "Request body is missing";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_TOO_LONG = "Name must not exceed 100 characters";
    public static final String DESCRIPTION_TOO_LONG = "Description must not exceed 500 characters";
    public static final String CHANNEL_ID_REQUIRED = "Channel ID cannot be null";
    public static final String INVALID_TIME_RANGE = "Time range must be one of: 1h, 6h, 12h, 24h, 7d, 30d";

    // Fields
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CONFIGURATION_CHANNEL_IDS = "configuration.channelIds";
    public static final String FILTERS_TIME_RANGE = "filters.timeRange";

    private static final Set<String> VALID_TIME_RANGES = Set.of("1h", "6h", "12h", "24h", "7d", "30d");

    /**
     * Validates a CreateWatchlistRequest.
     *
     * @param request the create request
     * @param result  the validation result
     */
    public void validate(final CreateWatchlistRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        validateNameAndDescription(request.getName(), request.getDescription(), result);
        validateConfiguration(request.getConfiguration(), result);
        validateFilters(request.getFilters(), result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * Validates an UpdateWatchlistRequest.
     *
     * @param request the update request
     * @param result  the validation result
     */
    public void validate(final UpdateWatchlistRequest request, final ValidationResult result) {
        if (isNull(request)) {
            result.reject(BODY_IS_MISSING);
            throw new ValidationException(result);
        }

        validateNameAndDescription(request.getName(), request.getDescription(), result);
        validateConfiguration(request.getConfiguration(), result);
        validateFilters(request.getFilters(), result);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (request instanceof CreateWatchlistRequest createRequest) {
            validate(createRequest, result);
        } else if (request instanceof UpdateWatchlistRequest updateRequest) {
            validate(updateRequest, result);
        } else {
            result.reject("Unsupported request type");
            throw new ValidationException(result);
        }
    }

    /**
     * Validates name and description fields.
     */
    private void validateNameAndDescription(
        final String name,
        final String description,
        final ValidationResult result
    ) {
        result.rejectField(NAME, name)
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(n -> n.length() > 100, NAME_TOO_LONG);

        result.rejectField(DESCRIPTION, description)
            .when(desc -> desc != null && desc.length() > 500, DESCRIPTION_TOO_LONG);
    }

    /**
     * Validates configuration (channel IDs).
     */
    private void validateConfiguration(
        final WatchlistConfigurationRequest configuration,
        final ValidationResult result
    ) {
        if (configuration == null || configuration.getChannelIds() == null) {
            return;
        }

        for (final Long channelId : configuration.getChannelIds()) {
            result.rejectField(CONFIGURATION_CHANNEL_IDS, channelId)
                .whenNull(CHANNEL_ID_REQUIRED);
        }
    }

    /**
     * Validates filter settings.
     */
    private void validateFilters(
        final WatchlistFiltersRequest filters,
        final ValidationResult result
    ) {
        if (filters == null) {
            return;
        }

        result.rejectField(FILTERS_TIME_RANGE, filters.getTimeRange())
            .when(
                timeRange -> timeRange != null && !VALID_TIME_RANGES.contains(timeRange),
                INVALID_TIME_RANGE
            );
    }
}
