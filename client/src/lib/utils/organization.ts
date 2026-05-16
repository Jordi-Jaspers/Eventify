import type { OrganizationStatus } from '$lib/api/models';

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

export function getOrganizationStatusLabel(status: OrganizationStatus | undefined): string {
	switch (status) {
		case 'ACTIVE':
			return 'Active';
		case 'SUSPENDED':
			return 'Suspended';
		default:
			return status ?? 'Unknown';
	}
}

export function getOwnerDisplayName(
	owner: { firstName?: string; lastName?: string } | undefined
): string {
	if (!owner || (!owner.firstName && !owner.lastName)) {
		return 'No owner';
	}
	return `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim();
}
