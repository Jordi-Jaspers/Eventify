import { client } from '../client';
import type { AdapterConfigResponse, CreateAdapterConfigRequest, UpdateAdapterConfigRequest } from '$lib/api/models';

// ====== Personal ======

export async function listPersonalConfigs(): Promise<AdapterConfigResponse[]> {
	const { data, error } = await client.GET('/v1/user/adapter-configs');
	if (error) throw error;
	return data;
}

export async function createPersonalConfig(
	req: CreateAdapterConfigRequest
): Promise<AdapterConfigResponse> {
	const { data, error } = await client.POST('/v1/user/adapter-configs', { body: req });
	if (error) throw error;
	return data;
}

export async function updatePersonalConfig(
	id: number,
	req: UpdateAdapterConfigRequest
): Promise<AdapterConfigResponse> {
	const { data, error } = await client.PUT('/v1/user/adapter-configs/{id}', {
		params: { path: { id } },
		body: req
	});
	if (error) throw error;
	return data;
}

export async function deletePersonalConfig(id: number): Promise<void> {
	const { error } = await client.DELETE('/v1/user/adapter-configs/{id}', {
		params: { path: { id } }
	});
	if (error) throw error;
}

export async function testAdapterConnection(adapterType: string, webhookUrl: string): Promise<{ success: boolean; error?: string }> {
	const { data, error } = await client.POST('/v1/adapter-configs/test', {
		body: { adapterType: adapterType as 'MATTERMOST' | 'SLACK', webhookUrl }
	});
	if (error) throw error;
	return data as { success: boolean; error?: string };
}

// ====== Organization ======

export async function listOrgConfigs(orgId: number): Promise<AdapterConfigResponse[]> {
	const { data, error } = await client.GET('/v1/organization/{orgId}/adapter-configs', {
		params: { path: { orgId } }
	});
	if (error) throw error;
	return data;
}

export async function createOrgConfig(
	orgId: number,
	req: CreateAdapterConfigRequest
): Promise<AdapterConfigResponse> {
	const { data, error } = await client.POST('/v1/organization/{orgId}/adapter-configs', {
		params: { path: { orgId } },
		body: req
	});
	if (error) throw error;
	return data;
}


