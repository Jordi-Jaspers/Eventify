import { client } from '$lib/api/client';
import type { RetentionSettingsResponse } from '$lib/api/models';

/**
 * Get an organization's retention settings
 */
export async function getOrganizationRetentionSettings(
	orgId: number
): Promise<RetentionSettingsResponse> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/settings/retention', {
		params: { path: { orgId } }
	});

	if (error) {
		throw error;
	}

	return data;
}

/**
 * Update an organization's retention settings
 */
export async function updateOrganizationRetentionSettings(
	orgId: number,
	retentionDays: number
): Promise<RetentionSettingsResponse> {
	const { data, error } = await client.PUT('/v1/organization/{orgId}/settings/retention', {
		params: { path: { orgId } },
		body: { retentionDays }
	});

	if (error) {
		throw error;
	}

	return data;
}
