<script lang="ts">
	import { Building2, Key, Database } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import { TabNav } from '$lib/components/ui/tab-nav';
	import type { Component } from 'svelte';

	interface Props {
		orgId: number;
		canManage?: boolean;
	}

	let { orgId, canManage = true }: Props = $props();

	interface OrgTab {
		label: string;
		path: string;
		icon: Component;
		adminOnly?: boolean;
	}

	const allTabs: OrgTab[] = $derived([
		{ label: 'General', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_GENERAL_PAGE(orgId).path, icon: Building2 },
		{ label: 'API Keys', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path, icon: Key },
		{ label: 'Data & Storage', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_DATA_STORAGE_PAGE(orgId).path, icon: Database, adminOnly: true }
	].filter((tab: OrgTab) => !tab.adminOnly || canManage));
</script>

<TabNav tabs={allTabs} ariaLabel="Organization settings navigation" />
