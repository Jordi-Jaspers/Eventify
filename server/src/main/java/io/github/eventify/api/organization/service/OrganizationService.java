package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.jframe.exception.core.ValidationException;
import io.github.jframe.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.organization.model.validator.OrganizationValidator.OWNER;
import static io.github.eventify.api.organization.model.validator.OrganizationValidator.OWNER_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static java.time.ZoneOffset.UTC;

/**
 * Service for managing organizations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private static final String HYPHEN = "-";

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    private final OrganizationMembershipRepository organizationMembershipRepository;

    /**
     * Create a new organization.
     *
     * @param request the organization provisioning request
     * @return the created organization
     */
    @Transactional
    public Organization create(final ProvisionOrganizationRequest request) {
        // Look up owner by email (owner is required, validated by OrganizationValidator)
        final String ownerEmail = request.getOwner();
        final Optional<User> ownerOpt = userRepository.findByEmail(ownerEmail);

        // Validate owner exists and is enabled
        final ValidationResult result = new ValidationResult();
        result.rejectField(OWNER, ownerEmail)
            .when(owner -> ownerOpt.isEmpty() || !ownerOpt.get().isEnabled(), OWNER_NOT_FOUND);

        if (result.hasErrors()) {
            throw new ValidationException(result);
        }

        final User owner = ownerOpt.get();

        final String slug = generateUniqueSlug(request.getName());

        final Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setSlug(slug);
        organization.setStatus(OrganizationStatus.TRIAL);
        organization.setCreatedBy(getLoggedInUser().getId());
        organization.setCreatedAt(OffsetDateTime.now(UTC));

        final Organization savedOrganization = organizationRepository.save(organization);

        // Create organization membership with OWNER role
        final OrganizationMembership membership = new OrganizationMembership();
        membership.setUser(owner);
        membership.setOrganization(savedOrganization);
        membership.setRole(OrganizationalRole.OWNER);
        membership.setInvitedBy(null);
        membership.setCreatedAt(OffsetDateTime.now(UTC));
        organizationMembershipRepository.save(membership);

        // Set transient owner field for response mapping
        savedOrganization.setOwner(owner);

        log.info(
            "Organization created: id={}, name={}, slug={}, createdBy={}, owner={}",
            savedOrganization.getId(),
            savedOrganization.getName(),
            savedOrganization.getSlug(),
            savedOrganization.getCreatedBy(),
            owner.getEmail()
        );

        return savedOrganization;
    }

    /**
     * Generate a unique slug from organization name. Handles collisions by appending -1, -2, etc.
     *
     * @param name the organization name
     * @return unique slug
     */
    private String generateUniqueSlug(final String name) {
        final String baseSlug = generateSlugFromName(name);

        final Optional<Organization> existingOrg = organizationRepository.findBySlug(baseSlug);
        if (existingOrg.isEmpty()) {
            return baseSlug;
        }

        int counter = 1;
        String candidateSlug = baseSlug + HYPHEN + counter;

        while (organizationRepository.findBySlug(candidateSlug).isPresent()) {
            counter++;
            candidateSlug = baseSlug + HYPHEN + counter;
        }

        return candidateSlug;
    }

    /**
     * Generate slug from organization name. Converts to lowercase, replaces spaces with hyphens, removes special characters, and collapses
     * multiple hyphens.
     *
     * @param name the organization name
     * @return generated slug
     */
    private String generateSlugFromName(final String name) {
        return name.toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", HYPHEN)
            .replaceAll("-+", HYPHEN)
            .replaceAll("^-|-$", "");
    }
}
