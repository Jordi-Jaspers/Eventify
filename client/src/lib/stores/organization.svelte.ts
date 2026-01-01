/**
 * Organization Store
 *
 * Manages organization context for the authenticated user
 * - Fetches user's organizations
 * - Persists current org ID in cookie via PersistentCookie
 * - Provides derived state for components
 */

import { getUserOrganizations } from '$lib/api/user/UserController';
import type { UserOrganizationResponse } from '$lib/api/models';
import { PersistentCookie } from '$lib/utils/persistent-cookie.svelte';
import { browser } from '$app/environment';

const COOKIE_NAME: string = 'currentOrganizationId';
const COOKIE_MAX_AGE: number = 2592000; // 30 days in seconds

class OrganizationStore {
	organizations: UserOrganizationResponse[] = $state([]);
	loading: boolean = $state(true);
	error: string | null = $state(null);
	
	private currentOrgIdCookie: PersistentCookie<string> = new PersistentCookie(
		COOKIE_NAME,
		'',
		COOKIE_MAX_AGE
	);

	get currentOrgId(): number | null {
		const value: string = this.currentOrgIdCookie.value;
		if (!value) return null;
		const parsed: number = parseInt(value, 10);
		return isNaN(parsed) ? null : parsed;
	}

	// Derived state
	currentOrganization: UserOrganizationResponse | null = $derived.by((): UserOrganizationResponse | null => {
		const orgId: number | null = this.currentOrgId;
		if (!orgId) return null;
		return (
			this.organizations.find(
				(org: UserOrganizationResponse) => org.organizationId === orgId
			) || null
		);
	});

	hasMultipleOrgs: boolean = $derived(this.organizations.length > 1);

	hasNoOrgs: boolean = $derived(this.organizations.length === 0);

	/**
	 * Initialize the store - fetch organizations and restore selected org from cookie
	 */
	async initialize(): Promise<void> {
		if (!browser) return;

		this.loading = true;
		this.error = null;

		try {
			const orgs: UserOrganizationResponse[] = await getUserOrganizations();
			this.organizations = orgs;

			// Try to restore org ID from cookie
			const cookieOrgId: number | null = this.currentOrgId;

			// Validate that cookie org exists in user's orgs
			if (cookieOrgId && orgs.some((org: UserOrganizationResponse) => org.organizationId === cookieOrgId)) {
				// Cookie is valid, keep it
			} else if (orgs.length > 0) {
				// Select first org if cookie invalid or not found
				this.currentOrgIdCookie.value = String(orgs[0].organizationId);
			} else {
				// No orgs - clear cookie
				this.currentOrgIdCookie.value = '';
			}
		} catch (err: unknown) {
			this.error = err instanceof Error ? err.message : 'Failed to load organizations';
			console.error('Failed to fetch organizations:', err);
		} finally {
			this.loading = false;
		}
	}

	/**
	 * Switch to a different organization
	 */
	switchOrganization(orgId: number): void {
		if (!browser) return;

		const org: UserOrganizationResponse | undefined = this.organizations.find(
			(o: UserOrganizationResponse) => o.organizationId === orgId
		);

		if (!org) {
			console.error(`Organization with ID ${orgId} not found`);
			return;
		}

		this.currentOrgIdCookie.value = String(orgId);
	}

	/**
	 * Refresh organizations from API
	 */
	async refreshOrganizations(): Promise<void> {
		await this.initialize();
	}
}

// Singleton instance
export const organizationStore: OrganizationStore = new OrganizationStore();
