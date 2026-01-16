<script lang="ts">
	import { Building2, Key, Database } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		currentPath: string;
		orgId: number;
		canManage?: boolean;
	}

	let { currentPath, orgId, canManage = true }: Props = $props();

	const tabs: Array<{ label: string; path: string; icon: typeof Building2; adminOnly?: boolean }> = $derived([
		{ label: 'General', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_GENERAL_PAGE(orgId).path, icon: Building2 },
		{ label: 'API Keys', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path, icon: Key },
		{ label: 'Data & Storage', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_DATA_STORAGE_PAGE(orgId).path, icon: Database, adminOnly: true }
	].filter((tab: { label: string; path: string; icon: typeof Building2; adminOnly?: boolean }) => !tab.adminOnly || canManage));
</script>

<div class="border-b border-border/50 bg-card/30 backdrop-blur-sm shadow-sm">
	<nav class="flex gap-4 px-6 max-w-4xl mx-auto" aria-label="Organization settings navigation">
		{#each tabs as tab}
			{@const isActive = currentPath === tab.path}
			{@const Icon = tab.icon}
			<a
				href={tab.path}
				class="flex items-center gap-2 px-4 py-3 border-b-2 transition-all {isActive
					? 'border-primary text-primary font-semibold'
					: 'border-transparent text-muted-foreground hover:text-foreground hover:border-border/50'}"
				aria-current={isActive ? 'page' : undefined}
			>
				<Icon class="w-4 h-4" />
				{tab.label}
			</a>
		{/each}
	</nav>
</div>
