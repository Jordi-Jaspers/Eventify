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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization subscription management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Subscriptions",
    description = "Manage watchlist subscriptions at organization level"
)
public class OrgSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionValidator subscriptionValidator;

    @PostMapping(
        path = ORGANIZATION_SUBSCRIPTIONS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Create org subscription",
        description = "Creates a subscription for the organization"
    )
    public ResponseEntity<SubscriptionResponse> createOrgSubscription(
        @PathVariable final Long orgId,
        @RequestBody final CreateSubscriptionRequest request
    ) {
        subscriptionValidator.validateAndThrow(request);
        final Subscription subscription = subscriptionService.createOrgSubscription(orgId, request);
        return ResponseEntity.status(CREATED).body(subscriptionMapper.toResourceObject(subscription));
    }

    @PutMapping(
        path = ORGANIZATION_SUBSCRIPTION_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Update org subscription",
        description = "Updates an org subscription by ID"
    )
    public ResponseEntity<SubscriptionResponse> updateOrgSubscription(
        @PathVariable final Long orgId,
        @PathVariable final Long id,
        @RequestBody final UpdateSubscriptionRequest request
    ) {
        subscriptionValidator.validateAndThrow(request);
        final Subscription subscription = subscriptionService.updateOrgSubscription(orgId, id, request);
        return ResponseEntity.status(OK).body(subscriptionMapper.toResourceObject(subscription));
    }

    @DeleteMapping(path = ORGANIZATION_SUBSCRIPTION_PATH)
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Delete org subscription",
        description = "Deletes an org subscription by ID"
    )
    public ResponseEntity<Void> deleteOrgSubscription(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        subscriptionService.deleteOrgSubscription(orgId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
        path = ORGANIZATION_SUBSCRIPTIONS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) || hasAnyAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Search org subscriptions",
        description = "Returns paginated subscriptions for the organization"
    )
    public ResponseEntity<PageResource<SubscriptionResponse>> searchOrgSubscriptions(
        @PathVariable final Long orgId,
        @RequestBody final SortablePageInput input
    ) {
        final Page<Subscription> page = subscriptionService.searchOrgSubscriptions(orgId, input);
        return ResponseEntity.status(OK).body(subscriptionMapper.toPageResource(page));
    }
}
