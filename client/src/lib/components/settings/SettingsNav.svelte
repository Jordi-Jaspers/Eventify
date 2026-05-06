<script lang="ts">
	import { User, Code2, Database, Shield, Link2 } from '@lucide/svelte';
	import { CLIENT_ROUTES } from '$lib/config/routes';

	interface Props {
		currentPath: string;
	}

	let { currentPath }: Props = $props();

	const tabs: Array<{ label: string; path: string; icon: typeof User }> = [
		{ label: 'Profile', path: CLIENT_ROUTES.PROFILE_PAGE.path, icon: User },
		{ label: 'Developer', path: CLIENT_ROUTES.DEVELOPER_PAGE.path, icon: Code2 },
		{ label: 'Data & Storage', path: CLIENT_ROUTES.DATA_STORAGE_PAGE.path, icon: Database },
		{ label: 'Sessions', path: CLIENT_ROUTES.PROFILE_SESSIONS_PAGE.path, icon: Shield },
		{ label: 'Connected Accounts', path: CLIENT_ROUTES.PROFILE_CONNECTED_ACCOUNTS_PAGE.path, icon: Link2 }
	];
</script>

<div class="border-b border-border/50 bg-card/30 backdrop-blur-sm">
	<nav class="flex gap-4 px-6 max-w-4xl mx-auto" aria-label="Settings navigation">
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
