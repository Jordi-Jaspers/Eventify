import type { OrganizationStatus } from '$lib/api/models';

/**
 * Get the badge variant for an organization status
 * - ACTIVE: success (green)
 * - SUSPENDED: destructive (red)
 */
export function getOrganizationStatusBadgeVariant(
	status: OrganizationStatus | undefined
): 'default' | 'success' | 'destructive' {
	switch (status) {
		case 'ACTIVE':
			return 'success';
		case 'SUSPENDED':
			return 'destructive';
		default:
			return 'default';
	}
}

/**
 * Get the display name for an organization owner
 * Returns "No owner" if owner is undefined or has no name
 */
export function getOwnerDisplayName(
	owner: { firstName?: string; lastName?: string } | undefined
): string {
	if (!owner || (!owner.firstName && !owner.lastName)) {
		return 'No owner';
	}
	return `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim();
}
