import { browser } from '$app/environment';
import { get } from 'svelte/store';
import { organizationStore } from '$lib/stores/organization.svelte';
import { currentUser } from '$lib/stores/auth';
import { getOrganizationById } from '$lib/api/admin/AdminOrganizationController';
import { isOrgAdmin } from '$lib/utils/role';
import type { UserOrganizationResponse, OrganizationResponse } from '$lib/api/models';

/**
 * Shared reactive context for org pages that need to handle both
 * regular org members and global admins viewing any org.
 *
 * Usage:
 *   const ctx = createAdminOrgContext(() => orgId);
 *   // ctx.canManage, ctx.orgName, ctx.isGlobalAdmin
 */
export function createAdminOrgContext(getOrgId: () => number): {
	readonly canManage: boolean;
	readonly isGlobalAdmin: boolean;
	readonly orgName: string;
	readonly organizationFromStore: UserOrganizationResponse | undefined;
	readonly adminFetchedOrg: OrganizationResponse | null;
} {
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let lastFetchedOrgId: number = $state(0);

	const isGlobalAdmin: boolean = $derived(get(currentUser)?.role === 'ADMIN');

	const organizationFromStore: UserOrganizationResponse | undefined = $derived(
		organizationStore.organizations.find(
			(org: UserOrganizationResponse) => org.organizationId === getOrgId()
		)
	);

	const canManage: boolean = $derived.by((): boolean => {
		if (isGlobalAdmin) return true;
		if (!organizationFromStore) return false;
		return isOrgAdmin(organizationFromStore.role);
	});

	const orgName: string = $derived.by((): string => {
		if (organizationFromStore) return organizationFromStore.organizationName ?? 'Organization';
		if (adminFetchedOrg) return adminFetchedOrg.name ?? 'Organization';
		return 'Organization';
	});

	$effect(() => {
		if (!browser) return;
		const currentOrgId: number = getOrgId();
		const needsFetch: boolean =
			isGlobalAdmin &&
			!organizationFromStore &&
			currentOrgId > 0 &&
			currentOrgId !== lastFetchedOrgId;

		if (needsFetch) {
			lastFetchedOrgId = currentOrgId;
			getOrganizationById(currentOrgId)
				.then((org: OrganizationResponse | null) => {
					adminFetchedOrg = org;
				})
				.catch(() => {
					adminFetchedOrg = null;
				});
		}
	});

	return {
		get canManage(): boolean {
			return canManage;
		},
		get isGlobalAdmin(): boolean {
			return isGlobalAdmin;
		},
		get orgName(): string {
			return orgName;
		},
		get organizationFromStore(): UserOrganizationResponse | undefined {
			return organizationFromStore;
		},
		get adminFetchedOrg(): OrganizationResponse | null {
			return adminFetchedOrg;
		}
	};
}

export type AdminOrgContext = ReturnType<typeof createAdminOrgContext>;
