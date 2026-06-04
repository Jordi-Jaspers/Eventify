import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';
import type { AdapterConfigResponse, CreateAdapterConfigRequest, UpdateAdapterConfigRequest } from '$lib/api/models';
import {
	listPersonalConfigs,
	createPersonalConfig,
	updatePersonalConfig,
	deletePersonalConfig,
	testAdapterConnection,
	listOrgConfigs,
	createOrgConfig
} from '../AdapterConfigController';

export function createAdapterConfigService(
	scope: 'personal' | 'organization',
	orgId?: number
) {
	let configs = $state<AdapterConfigResponse[]>([]);
	let loading = $state(false);
	let saving = $state(false);

	async function loadConfigs(): Promise<void> {
		loading = true;
		try {
			if (scope === 'personal') {
				configs = await listPersonalConfigs();
			} else {
				if (!orgId) throw new Error('orgId required for org scope');
				configs = await listOrgConfigs(orgId);
			}
		} catch (err) {
			const { message } = handleError(err, 'Failed to load adapter configs');
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function addConfig(req: CreateAdapterConfigRequest): Promise<void> {
		saving = true;
		try {
			let created: AdapterConfigResponse;
			if (scope === 'personal') {
				created = await createPersonalConfig(req);
			} else {
				if (!orgId) throw new Error('orgId required for org scope');
				created = await createOrgConfig(orgId, req);
			}
			configs = [...configs, created];
			toast.success('Adapter added successfully');
		} catch (err) {
			const { message } = handleError(err, 'Failed to add adapter');
			toast.error(message);
			throw err;
		} finally {
			saving = false;
		}
	}

	async function updateConfig(id: number, req: UpdateAdapterConfigRequest): Promise<void> {
		saving = true;
		try {
			const updated = await updatePersonalConfig(id, req);
			configs = configs.map((c) => (c.id === id ? updated : c));
			toast.success('Adapter updated successfully');
		} catch (err) {
			const { message } = handleError(err, 'Failed to update adapter');
			toast.error(message);
			throw err;
		} finally {
			saving = false;
		}
	}

	async function deleteConfig(id: number): Promise<void> {
		saving = true;
		try {
			await deletePersonalConfig(id);
			configs = configs.filter((c) => c.id !== id);
			toast.success('Adapter removed');
		} catch (err) {
			const { message } = handleError(err, 'Failed to remove adapter');
			toast.error(message);
		} finally {
			saving = false;
		}
	}

	async function testConfig(adapterType: string, webhookUrl: string): Promise<void> {
		try {
			const result = await testAdapterConnection(adapterType, webhookUrl);
			if (result.success) {
				toast.success('Test notification sent successfully');
			} else {
				toast.error(parseTestError(result.error));
			}
		} catch (err) {
			const { message } = handleError(err, 'Test notification failed');
			toast.error(message);
			throw err;
		}
	}

	function parseTestError(error?: string): string {
		if (!error) return 'Test notification failed';
		try {
			// Error may contain nested JSON like: '400 Bad Request: "{...}"'
			const jsonMatch = error.match(/\{.*\}/s);
			if (jsonMatch) {
				const parsed = JSON.parse(jsonMatch[0]);
				if (parsed.message) return parsed.message;
			}
		} catch { /* fall through */ }
		// Strip HTTP status prefix if present
		return error.replace(/^\d{3}\s+[^:]+:\s*"?/, '').replace(/"$/, '') || 'Test notification failed';
	}

	return {
		get configs() { return configs; },
		get loading() { return loading; },
		get saving() { return saving; },
		loadConfigs,
		addConfig,
		updateConfig,
		deleteConfig,
		testConfig
	};
}
