import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';
import {
	getUserRetentionSettings,
	updateUserRetentionSettings
} from '$lib/api/user/UserSettingsController';
import {
	getOrganizationRetentionSettings,
	updateOrganizationRetentionSettings
} from '$lib/api/organization/OrganizationSettingsController';
import type { RetentionSettingsResponse } from '$lib/api/models';

const DEFAULT_RETENTION_DAYS: number = 365;

/**
 * Service for managing retention settings state and operations.
 * Encapsulates loading, saving, and state management for data retention configuration.
 */
export function createRetentionService(type: 'user' | 'organization', orgId?: number) {
	let loading: boolean = $state(true);
	let saving: boolean = $state(false);
	let retentionDays: number = $state(DEFAULT_RETENTION_DAYS);
	let savedRetentionDays: number = $state(DEFAULT_RETENTION_DAYS);

	/**
	 * Load retention settings from API
	 */
	async function loadSettings(): Promise<void> {
		loading = true;
		try {
			let data: RetentionSettingsResponse;

			if (type === 'user') {
				data = await getUserRetentionSettings();
			} else {
				if (!orgId) {
					throw new Error('Organization ID is required');
				}
				data = await getOrganizationRetentionSettings(orgId);
			}

			retentionDays = data.retentionDays ?? DEFAULT_RETENTION_DAYS;
			savedRetentionDays = retentionDays;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to load retention settings'
			);
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	/**
	 * Save retention settings to API
	 */
	async function saveSettings(days: number): Promise<void> {
		saving = true;
		try {
			let data: RetentionSettingsResponse;

			if (type === 'user') {
				data = await updateUserRetentionSettings(days);
			} else {
				if (!orgId) {
					throw new Error('Organization ID is required');
				}
				data = await updateOrganizationRetentionSettings(orgId, days);
			}

			retentionDays = data.retentionDays ?? days;
			savedRetentionDays = retentionDays;
			toast.success('Retention settings updated successfully');
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(
				err,
				'Failed to update retention settings'
			);
			toast.error(message);
		} finally {
			saving = false;
		}
	}

	return {
		// State getters
		get loading(): boolean {
			return loading;
		},
		get saving(): boolean {
			return saving;
		},
		get retentionDays(): number {
			return retentionDays;
		},
		get savedRetentionDays(): number {
			return savedRetentionDays;
		},

		// Actions
		loadSettings,
		saveSettings
	};
}

export type RetentionService = ReturnType<typeof createRetentionService>;
