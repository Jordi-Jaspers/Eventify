<script lang="ts">
	import { Building2, Key } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		currentPath: string;
		orgId: number;
	}

	let { currentPath, orgId }: Props = $props();

	const tabs: Array<{ label: string; path: string; icon: typeof Building2 }> = $derived([
		{ label: 'General', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_GENERAL_PAGE(orgId).path, icon: Building2 },
		{ label: 'API Keys', path: CLIENT_ROUTES.ORGANIZATION_SETTINGS_API_KEYS_PAGE(orgId).path, icon: Key }
	]);
</script>

<div class="border-b border-border/50 bg-card/30 backdrop-blur-sm">
	<nav class="flex gap-4 px-6 max-w-4xl mx-auto" aria-label="Organization settings navigation">
		{#each tabs as tab}
			{@const isActive = currentPath === tab.path}
			{@const Icon = tab.icon}
			<a
				href={tab.path}
				class="flex items-center gap-2 px-4 py-3 border-b-2 transition-colors {isActive
					? 'border-primary text-primary font-semibold'
					: 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'}"
				aria-current={isActive ? 'page' : undefined}
			>
				<Icon class="w-4 h-4" />
				{tab.label}
			</a>
		{/each}
	</nav>
</div>
