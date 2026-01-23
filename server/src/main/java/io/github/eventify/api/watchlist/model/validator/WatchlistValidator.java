package io.github.eventify.api.watchlist.model.validator;

import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
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
    public static final String INVALID_TIME_RANGE = "Time range must be one of: 24h, 7d, 30d";

    // Fields
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CHANNEL_IDS = "channelIds";
    public static final String DEFAULT_TIME_RANGE = "defaultTimeRange";

    private static final Set<String> VALID_TIME_RANGES = Set.of("24h", "7d", "30d");

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

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(name -> name.length() > 100, NAME_TOO_LONG);

        result.rejectField(DESCRIPTION, request.getDescription())
            .when(desc -> desc != null && desc.length() > 500, DESCRIPTION_TOO_LONG);

        // Validate channel IDs (no nulls in list)
        if (request.getChannelIds() != null) {
            for (int i = 0; i < request.getChannelIds().size(); i++) {
                final Long channelId = request.getChannelIds().get(i);
                result.rejectField(CHANNEL_IDS, channelId)
                    .whenNull(CHANNEL_ID_REQUIRED);
            }
        }

        // Validate default time range
        result.rejectField(DEFAULT_TIME_RANGE, request.getDefaultTimeRange())
            .when(
                timeRange -> timeRange != null && !VALID_TIME_RANGES.contains(timeRange),
                INVALID_TIME_RANGE
            );

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

        result.rejectField(NAME, request.getName())
            .whenNull(NAME_REQUIRED)
            .orWhen(String::isEmpty, NAME_REQUIRED)
            .orWhen(String::isBlank, NAME_REQUIRED)
            .orWhen(name -> name.length() > 100, NAME_TOO_LONG);

        result.rejectField(DESCRIPTION, request.getDescription())
            .when(desc -> desc != null && desc.length() > 500, DESCRIPTION_TOO_LONG);

        // Validate channel IDs (no nulls in list)
        if (request.getChannelIds() != null) {
            for (int i = 0; i < request.getChannelIds().size(); i++) {
                final Long channelId = request.getChannelIds().get(i);
                result.rejectField(CHANNEL_IDS, channelId)
                    .whenNull(CHANNEL_ID_REQUIRED);
            }
        }

        // Validate default time range
        result.rejectField(DEFAULT_TIME_RANGE, request.getDefaultTimeRange())
            .when(
                timeRange -> timeRange != null && !VALID_TIME_RANGES.contains(timeRange),
                INVALID_TIME_RANGE
            );

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(final Object request, final ValidationResult result) {
        if (request instanceof CreateWatchlistRequest) {
            validate((CreateWatchlistRequest) request, result);
        } else if (request instanceof UpdateWatchlistRequest) {
            validate((UpdateWatchlistRequest) request, result);
        } else {
            result.reject("Unsupported request type");
            throw new ValidationException(result);
        }
    }
}
