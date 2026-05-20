import { browser } from '$app/environment';
import type {
	UserOrganizationResponse,
	OrganizationResponse,
	OrganizationalRole
} from '$lib/api/models';
import { getOrganizationById } from '$lib/api/admin/AdminOrganizationController';

/**
 * Consolidated organization details state
 */
export interface OrganizationDetails {
	name: string;
	slug: string;
	role?: OrganizationalRole;
	joinedAt?: string;
	createdAt?: string;
	ownerName: string;
	memberCount?: number;
}

/**
 * Service for fetching and managing organization details.
 * Handles both member view (from store) and global admin view (from API).
 */
export function createOrganizationDetailsService(
	orgId: number,
	organizationFromStore: UserOrganizationResponse | undefined,
	isGlobalAdmin: boolean
) {
	let adminFetchedOrg: OrganizationResponse | null = $state(null);
	let isLoadingAdminOrg: boolean = $state(false);
	let lastFetchedOrgId: number = $state(0);

	// Fetch org for global admin if not in store
	$effect((): void => {
		if (!browser) return;
		const currentOrgId: number = orgId;
		const needsFetch: boolean =
			isGlobalAdmin && !organizationFromStore && currentOrgId > 0 && currentOrgId !== lastFetchedOrgId;

		if (needsFetch) {
			isLoadingAdminOrg = true;
			lastFetchedOrgId = currentOrgId;
			getOrganizationById(currentOrgId)
				.then((org: OrganizationResponse | null): void => {
					adminFetchedOrg = org;
				})
				.catch((): void => {
					adminFetchedOrg = null;
				})
				.finally((): void => {
					isLoadingAdminOrg = false;
				});
		}
	});

	// Derived computed states
	const hasOrgFromStore: boolean = $derived(!!organizationFromStore);
	const hasAdminFetchedOrg: boolean = $derived(adminFetchedOrg !== null);

	const details: OrganizationDetails = $derived.by((): OrganizationDetails => {
		if (organizationFromStore) {
			return {
				name: organizationFromStore.organizationName ?? 'Unknown Organization',
				slug: organizationFromStore.organizationSlug ?? 'N/A',
				role: organizationFromStore.role,
				joinedAt: organizationFromStore.joinedAt,
				createdAt: undefined,
				ownerName: '',
				memberCount: undefined
			};
		}
		if (adminFetchedOrg) {
			const owner = adminFetchedOrg.owner;
			const ownerName: string = owner
				? `${owner.firstName ?? ''} ${owner.lastName ?? ''}`.trim() || 'Unknown'
				: '';
			return {
				name: adminFetchedOrg.name ?? 'Unknown Organization',
				slug: adminFetchedOrg.slug ?? 'N/A',
				role: undefined,
				joinedAt: undefined,
				createdAt: adminFetchedOrg.createdAt,
				ownerName,
				memberCount: adminFetchedOrg.memberCount
			};
		}
		return {
			name: 'Unknown Organization',
			slug: 'N/A',
			role: undefined,
			joinedAt: undefined,
			createdAt: undefined,
			ownerName: '',
			memberCount: undefined
		};
	});

	return {
		get isLoadingAdminOrg(): boolean {
			return isLoadingAdminOrg;
		},
		get hasOrgFromStore(): boolean {
			return hasOrgFromStore;
		},
		get hasAdminFetchedOrg(): boolean {
			return hasAdminFetchedOrg;
		},
		get details(): OrganizationDetails {
			return details;
		}
	};
}

export type OrganizationDetailsService = ReturnType<typeof createOrganizationDetailsService>;
