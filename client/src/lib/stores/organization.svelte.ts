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
import { browser } from '$app/environment';

const COOKIE_NAME: string = 'currentOrganizationId';
const COOKIE_MAX_AGE: number = 2592000; // 30 days in seconds

/**
 * Read cookie value from document.cookie
 */
function readCookie(key: string): string | null {
	if (!browser) return null;

	const cookies: string[] = document.cookie.split(';');
	for (const cookie of cookies) {
		const [name, ...valueParts] = cookie.trim().split('=');
		if (name === key) {
			return valueParts.join('=');
		}
	}
	return null;
}

/**
 * Write cookie value to document.cookie
 */
function writeCookie(key: string, value: string, maxAge: number): void {
	if (!browser) return;
	document.cookie = `${key}=${value}; path=/; max-age=${maxAge}`;
}

class OrganizationStore {
	organizations: UserOrganizationResponse[] = $state([]);
	loading: boolean = $state(true);
	error: string | null = $state(null);
	
	// Store the org ID as reactive state directly
	private _currentOrgId: string = $state('');

	constructor() {
		// Read from cookie on initialization (client-side only)
		if (browser) {
			const cookieValue: string | null = readCookie(COOKIE_NAME);
			this._currentOrgId = cookieValue || '';
		}
	}

	// Derived: parse the org ID as number
	currentOrgId: number | null = $derived.by((): number | null => {
		if (!this._currentOrgId) return null;
		const parsed: number = parseInt(this._currentOrgId, 10);
		return isNaN(parsed) ? null : parsed;
	});

	// Derived: find the current organization object
	currentOrganization: UserOrganizationResponse | null = $derived.by((): UserOrganizationResponse | null => {
		const orgId: number | null = this.currentOrgId;
		if (!orgId) return null;
		return (
			this.organizations.find(
				(org: UserOrganizationResponse) => org.organizationId === orgId
			) || null
		);
		// TODO: When UserOrganizationResponse includes a `status` field, auto-switch to null (personal context)
		// and show toast.error if the current organization's status becomes SUSPENDED.
	});

	hasMultipleOrgs: boolean = $derived(this.organizations.length > 1);

	hasNoOrgs: boolean = $derived(this.organizations.length === 0);

	// Derived: get the current user's role in the selected organization
	currentRole: 'OWNER' | 'ADMIN' | 'MEMBER' | null = $derived.by((): 'OWNER' | 'ADMIN' | 'MEMBER' | null => {
		const org: UserOrganizationResponse | null = this.currentOrganization;
		if (!org || !org.role) return null;
		return org.role;
	});

	/**
	 * Set the current org ID (updates both state and cookie)
	 */
	private setCurrentOrgId(orgId: string): void {
		this._currentOrgId = orgId;
		writeCookie(COOKIE_NAME, orgId, COOKIE_MAX_AGE);
	}

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

			// Try to restore org ID from cookie (already loaded in constructor)
			const cookieOrgId: number | null = this.currentOrgId;

			// Validate that cookie org exists in user's orgs
			if (cookieOrgId && orgs.some((org: UserOrganizationResponse) => org.organizationId === cookieOrgId)) {
				// Cookie is valid, keep it
			} else if (orgs.length > 0) {
				// Select first org if cookie invalid or not found
				this.setCurrentOrgId(String(orgs[0].organizationId));
			} else {
				// No orgs - clear cookie
				this.setCurrentOrgId('');
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

		this.setCurrentOrgId(String(orgId));
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
