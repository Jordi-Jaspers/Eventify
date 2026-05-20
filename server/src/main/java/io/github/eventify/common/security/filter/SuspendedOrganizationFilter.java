package io.github.eventify.common.security.filter;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.common.config.RequestMatcherConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.server.PathContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;

/**
 * Filter that blocks access to organization-scoped endpoints when the organization is suspended.
 * Admin users with MANAGE_ORGANIZATIONS authority are allowed through.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuspendedOrganizationFilter extends OncePerRequestFilter {

    private static final String SUSPENDED_MESSAGE = "Organization has been suspended";
    private static final String ORG_ID_VARIABLE = "orgId";

    private final OrganizationRepository organizationRepository;

    @Override
    protected boolean shouldNotFilter(@NonNull final HttpServletRequest request) {
        final boolean isPublic = RequestMatcherConfig.getPublicMatchers().stream()
            .anyMatch(matcher -> matcher.matches(request));
        final boolean isAdmin = RequestMatcherConfig.getAdminMatchers().stream()
            .anyMatch(matcher -> matcher.matches(request));
        return isPublic || isAdmin;
    }

    @Override
    protected void doFilterInternal(
        @NonNull final HttpServletRequest request,
        @NonNull final HttpServletResponse response,
        @NonNull final FilterChain filterChain) throws ServletException, IOException {

        final Long orgId = extractOrgId(request);
        if (orgId != null && isSuspendedAndBlocked(orgId)) {
            log.debug("Blocking access to suspended organization from URI {}", request.getRequestURI());
            SecurityResponseHandler.handleForbiddenAccess(request, response, SUSPENDED_MESSAGE);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private Long extractOrgId(final HttpServletRequest request) {
        if (!RequestMatcherConfig.getOrgMatcher().matches(request)) {
            return null;
        }
        final PathContainer path = PathContainer.parsePath(request.getRequestURI());
        final PathPattern.PathMatchInfo matchInfo = RequestMatcherConfig.getOrgPathPattern().matchAndExtract(path);
        final String orgIdStr = matchInfo != null ? matchInfo.getUriVariables().get(ORG_ID_VARIABLE) : null;
        return orgIdStr != null ? Long.parseLong(orgIdStr) : null;
    }

    private boolean isSuspendedAndBlocked(final Long orgId) {
        final Optional<Organization> orgOpt = organizationRepository.findById(orgId);
        if (orgOpt.isEmpty() || orgOpt.get().getStatus() != OrganizationStatus.SUSPENDED) {
            return false;
        }
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || !hasManageOrganizationsAuthority(authentication);
    }

    private boolean hasManageOrganizationsAuthority(final Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(Permission.MANAGE_ORGANIZATIONS.name()::equals);
    }
}
