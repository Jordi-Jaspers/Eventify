package io.github.eventify.api.subscription.controller;

import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.mapper.SubscriptionMapper;
import io.github.eventify.api.subscription.model.request.CreateSubscriptionRequest;
import io.github.eventify.api.subscription.model.request.UpdateSubscriptionRequest;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.subscription.model.validator.SubscriptionValidator;
import io.github.eventify.api.subscription.service.SubscriptionService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for personal subscription management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Subscriptions",
    description = "Manage personal watchlist subscriptions"
)
public class UserSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionValidator subscriptionValidator;

    @PostMapping(
        path = SUBSCRIPTIONS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create personal subscription",
        description = "Creates or upserts a personal subscription for the authenticated user"
    )
    public ResponseEntity<SubscriptionResponse> createPersonalSubscription(@RequestBody final CreateSubscriptionRequest request) {
        subscriptionValidator.validateAndThrow(request);
        final Subscription subscription = subscriptionService.createPersonalSubscription(request);
        return ResponseEntity.status(CREATED).body(subscriptionMapper.toResourceObject(subscription));
    }

    @PutMapping(
        path = SUBSCRIPTION_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "Update personal subscription",
        description = "Updates a personal subscription by ID"
    )
    public ResponseEntity<SubscriptionResponse> updatePersonalSubscription(
        @PathVariable final Long id,
        @RequestBody final UpdateSubscriptionRequest request
    ) {
        subscriptionValidator.validateAndThrow(request);
        final Subscription subscription = subscriptionService.updatePersonalSubscription(id, request);
        return ResponseEntity.status(OK).body(subscriptionMapper.toResourceObject(subscription));
    }

    @DeleteMapping(path = SUBSCRIPTION_PATH)
    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Delete personal subscription",
        description = "Deletes a personal subscription by ID"
    )
    public ResponseEntity<Void> deletePersonalSubscription(@PathVariable final Long id) {
        subscriptionService.deletePersonalSubscription(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
        path = SUBSCRIPTIONS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "Search personal subscriptions",
        description = "Returns paginated personal subscriptions for the authenticated user"
    )
    public ResponseEntity<PageResource<SubscriptionResponse>> searchPersonalSubscriptions(
        @RequestBody final SortablePageInput input
    ) {
        final Page<Subscription> page = subscriptionService.searchPersonalSubscriptions(input);
        return ResponseEntity.status(OK).body(subscriptionMapper.toPageResource(page));
    }
}
