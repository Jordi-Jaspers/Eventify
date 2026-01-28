package io.github.eventify.api.watchlist.model.validator;

import io.github.eventify.api.watchlist.model.request.ChannelGroupRequest;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import io.github.jframe.validation.Validator;

import java.util.HashSet;
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
    public static final String GROUP_NAME_REQUIRED = "Group name is required";
    public static final String GROUP_NAME_TOO_LONG = "Group name must not exceed 100 characters";
    public static final String DUPLICATE_CHANNEL_ID = "Channel ID %d appears multiple times in configuration";

    // Fields
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CONFIGURATION_CHANNEL_IDS = "configuration.channelIds";
    public static final String CONFIGURATION_GROUPS = "configuration.groups";

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
     * Validates configuration (channel IDs and groups).
     */
    private void validateConfiguration(
        final WatchlistConfigurationRequest configuration,
        final ValidationResult result
    ) {
        if (configuration == null) {
            return;
        }

        validateChannelIds(configuration, result);
        validateGroups(configuration, result);
        validateNoDuplicateChannels(configuration, result);
    }

    /**
     * Validates standalone channel IDs.
     */
    private void validateChannelIds(
        final WatchlistConfigurationRequest configuration,
        final ValidationResult result
    ) {
        if (configuration.getChannelIds() == null) {
            return;
        }

        for (final Long channelId : configuration.getChannelIds()) {
            result.rejectField(CONFIGURATION_CHANNEL_IDS, channelId)
                .whenNull(CHANNEL_ID_REQUIRED);
        }
    }

    /**
     * Validates channel groups.
     * Note: Nested groups are not allowed - ChannelGroupRequest only has channelIds, not sub-groups.
     */
    private void validateGroups(
        final WatchlistConfigurationRequest configuration,
        final ValidationResult result
    ) {
        if (configuration.getGroups() == null) {
            return;
        }

        int groupIndex = 0;
        for (final ChannelGroupRequest group : configuration.getGroups()) {
            final String groupField = CONFIGURATION_GROUPS + "[" + groupIndex + "]";

            // Validate group name
            result.rejectField(groupField + ".name", group.getName())
                .whenNull(GROUP_NAME_REQUIRED)
                .orWhen(String::isEmpty, GROUP_NAME_REQUIRED)
                .orWhen(String::isBlank, GROUP_NAME_REQUIRED)
                .orWhen(n -> n.length() > 100, GROUP_NAME_TOO_LONG);

            // Validate channel IDs within group
            if (group.getChannelIds() != null) {
                for (final Long channelId : group.getChannelIds()) {
                    result.rejectField(groupField + ".channelIds", channelId)
                        .whenNull(CHANNEL_ID_REQUIRED);
                }
            }

            groupIndex++;
        }
    }

    /**
     * Validates that no channel ID appears more than once across the entire configuration.
     */
    private void validateNoDuplicateChannels(
        final WatchlistConfigurationRequest configuration,
        final ValidationResult result
    ) {
        if (configuration == null) {
            return;
        }

        final Set<Long> seenChannelIds = new HashSet<>();
        checkStandaloneChannelsForDuplicates(configuration, seenChannelIds, result);
        checkGroupChannelsForDuplicates(configuration, seenChannelIds, result);
    }

    /**
     * Checks standalone channels for duplicates.
     */
    private void checkStandaloneChannelsForDuplicates(
        final WatchlistConfigurationRequest configuration,
        final Set<Long> seenChannelIds,
        final ValidationResult result
    ) {
        if (configuration.getChannelIds() == null) {
            return;
        }

        for (final Long channelId : configuration.getChannelIds()) {
            rejectIfDuplicate(channelId, seenChannelIds, result);
        }
    }

    /**
     * Checks channels within groups for duplicates.
     */
    private void checkGroupChannelsForDuplicates(
        final WatchlistConfigurationRequest configuration,
        final Set<Long> seenChannelIds,
        final ValidationResult result
    ) {
        if (configuration.getGroups() == null) {
            return;
        }

        for (final ChannelGroupRequest group : configuration.getGroups()) {
            checkGroupForDuplicates(group, seenChannelIds, result);
        }
    }

    /**
     * Checks a single group's channels for duplicates.
     */
    private void checkGroupForDuplicates(
        final ChannelGroupRequest group,
        final Set<Long> seenChannelIds,
        final ValidationResult result
    ) {
        if (group.getChannelIds() == null) {
            return;
        }

        for (final Long channelId : group.getChannelIds()) {
            rejectIfDuplicate(channelId, seenChannelIds, result);
        }
    }

    /**
     * Rejects a channel ID if it's a duplicate.
     */
    private void rejectIfDuplicate(
        final Long channelId,
        final Set<Long> seenChannelIds,
        final ValidationResult result
    ) {
        if (channelId != null && !seenChannelIds.add(channelId)) {
            result.reject(String.format(DUPLICATE_CHANNEL_ID, channelId));
        }
    }
}
