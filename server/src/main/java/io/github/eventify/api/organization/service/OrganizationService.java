package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Create a new organization.
     *
     * @param request the organization provisioning request
     * @return the created organization
     */
    @Transactional
    public Organization create(final ProvisionOrganizationRequest request) {
        final String slug = generateUniqueSlug(request.getName());

        final Organization organization = new Organization();
        organization.setName(request.getName());
        organization.setSlug(slug);
        organization.setStatus(OrganizationStatus.TRIAL);
        organization.setCreatedBy(getLoggedInUser().getId());
        organization.setCreatedAt(OffsetDateTime.now(UTC));

        final Organization savedOrganization = organizationRepository.save(organization);
        log.info(
            "Organization created: id={}, name={}, slug={}, createdBy={}",
            savedOrganization.getId(),
            savedOrganization.getName(),
            savedOrganization.getSlug(),
            savedOrganization.getCreatedBy()
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
