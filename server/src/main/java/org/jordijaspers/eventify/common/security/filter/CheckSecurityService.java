package org.jordijaspers.eventify.common.security.filter;

import lombok.RequiredArgsConstructor;

import org.jordijaspers.eventify.api.source.model.Source;
import org.jordijaspers.eventify.api.source.service.SourceService;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.springframework.stereotype.Service;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CANNOT_ACCESS_CHECK;
import static org.jordijaspers.eventify.common.util.SecurityUtil.getLoggedInSource;

/**
 * The CheckSecurityService provides security filters for the check, validating the if the source has access to a particular check.
 */
@Service
@RequiredArgsConstructor
public class CheckSecurityService {

    private final SourceService sourceService;

    /**
     * Check if the source has access to the check.
     *
     * @param checkId The check id to check access for
     * @return true if the source has access to the check
     */
    public boolean hasCheckPermission(final Long checkId) {
        final Source source = getLoggedInSource();
        if (!sourceService.containsCheck(source.getId(), checkId)) {
            throw new AuthorizationException(CANNOT_ACCESS_CHECK);
        }
        return true;
    }
}
