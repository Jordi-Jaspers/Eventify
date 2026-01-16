import { client } from '$lib/api/client';
import type { RetentionSettingsResponse } from '$lib/api/models';

/**
 * Get the current user's retention settings
 */
export async function getUserRetentionSettings(): Promise<RetentionSettingsResponse> {
	const { data, error } = await client.GET('/v1/user/settings/retention');

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Update the current user's retention settings
 */
export async function updateUserRetentionSettings(
	retentionDays: number
): Promise<RetentionSettingsResponse> {
	const { data, error } = await client.PUT('/v1/user/settings/retention', {
		body: { retentionDays }
	});

	if (error) {
		throw error;
	}

	return data;
}
