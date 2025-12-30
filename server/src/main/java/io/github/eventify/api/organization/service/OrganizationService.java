package io.github.eventify.api.organization.service;

import io.github.eventify.api.organization.model.OrganisationMetaData;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.request.ProvisionOrganizationRequest;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.exception.NonExistingUserException;
import io.github.jframe.datasource.search.model.JpaSearchSpecification;
import io.github.jframe.datasource.search.model.SearchCriterium;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.organization.model.OrganizationalRole.OWNER;
import static io.github.jframe.util.constants.Constants.Characters.HYPHEN;

/**
 * Service for managing organizations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final UserRepository userRepository;

    private final OrganisationMetaData organizationMetaData;

    private final OrganizationRepository organizationRepository;

    private final OrganizationMembershipRepository organizationMembershipRepository;

    /**
     * Search and filter organizations with pagination using SortablePageInput.
     *
     * @param input pagination and search parameters
     * @return paginated list of organizations
     */
    @Transactional(readOnly = true)
    public Page<Organization> searchOrganizations(final SortablePageInput input) {
        final Sort sort = organizationMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);

        final List<SearchCriterium> criteria = organizationMetaData.toSearchCriteria(input.getSearchInputs());
        final Specification<Organization> spec = new JpaSearchSpecification<>(criteria);
        return organizationRepository.findAll(spec, pageable)
            .map(org -> {
                final int memberCount = organizationRepository.countMembersByOrganizationId(org.getId());
                org.setMemberCount(memberCount);
                return org;
            });
    }

    /**
     * Create a new organization.
     *
     * @param request the organization provisioning request
     * @return the created organization
     */
    @Transactional
    public Organization create(final ProvisionOrganizationRequest request) {
        final User owner = userRepository.findByEmail(request.getOwner())
            .filter(User::isEnabled)
            .orElseThrow(NonExistingUserException::new);

        final Organization organization = organizationRepository.save(
            new Organization(
                request.getName(),
                generateUniqueSlug(request.getName())
            )
        );

        organizationMembershipRepository.save(new OrganizationMembership(organization, owner, OWNER));
        organization.setOwner(owner);
        return organization;
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
     * @return generated slug
     */
    private String generateSlugFromName(final String name) {
        return name.toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", HYPHEN.toString())
            .replaceAll("-+", HYPHEN.toString())
            .replaceAll("^-|-$", "");
    }
}
