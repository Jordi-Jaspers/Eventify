package io.github.eventify.api.subscription.controller;

import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.mapper.SubscriptionMapper;
import io.github.eventify.api.subscription.model.request.SubscribeRequest;
import io.github.eventify.api.subscription.model.response.SubscriptionResponse;
import io.github.eventify.api.subscription.model.validator.SubscriptionValidator;
import io.github.eventify.api.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for watchlist subscription management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Subscriptions",
    description = "Manage watchlist subscriptions for severity notifications"
)
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionValidator subscriptionValidator;

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Subscribe to watchlist",
        description = "Creates or updates a subscription for the authenticated user on the given watchlist"
    )
    @PostMapping(
        path = USER_WATCHLISTS_PATH + "/{watchlistId}" + SUBSCRIPTION_PART,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@watchlistSecurity.canAccessUserWatchlist(#watchlistId, principal.user.id)")
    public ResponseEntity<SubscriptionResponse> subscribe(
        @PathVariable final Long watchlistId,
        @RequestBody final SubscribeRequest request
    ) {
        subscriptionValidator.validateAndThrow(request);
        final Subscription subscription = subscriptionService.subscribe(watchlistId, request);
        return ResponseEntity.status(CREATED).body(subscriptionMapper.toResourceObject(subscription));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get subscription",
        description = "Returns the subscription for the authenticated user on the given watchlist"
    )
    @GetMapping(
        path = USER_WATCHLISTS_PATH + "/{watchlistId}" + SUBSCRIPTION_PART,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@watchlistSecurity.canAccessUserWatchlist(#watchlistId, principal.user.id)")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable final Long watchlistId) {
        final Subscription subscription = subscriptionService.getSubscription(watchlistId);
        return ResponseEntity.status(OK).body(subscriptionMapper.toResourceObject(subscription));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Unsubscribe from watchlist",
        description = "Deletes the subscription for the authenticated user on the given watchlist"
    )
    @DeleteMapping(path = USER_WATCHLISTS_PATH + "/{watchlistId}" + SUBSCRIPTION_PART)
    @PreAuthorize("@watchlistSecurity.canAccessUserWatchlist(#watchlistId, principal.user.id)")
    public ResponseEntity<Void> unsubscribe(@PathVariable final Long watchlistId) {
        subscriptionService.unsubscribe(watchlistId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
