<script lang="ts">
	import { page } from '$app/stores';
	import type { Component } from 'svelte';

	interface Tab {
		label: string;
		path: string;
		icon?: Component;
	}

	interface Props {
		tabs: Tab[];
		ariaLabel?: string;
	}

	let { tabs, ariaLabel = 'Navigation' }: Props = $props();
</script>

<div class="border-b border-border/50 bg-card/30 backdrop-blur-sm shadow-sm">
	<nav class="flex gap-4 px-6 max-w-4xl mx-auto" aria-label={ariaLabel}>
		{#each tabs as tab}
			{@const isActive = $page.url.pathname.startsWith(tab.path)}
			<a
				href={tab.path}
				class="flex items-center gap-2 px-4 py-3 border-b-2 transition-all {isActive
					? 'border-primary text-primary font-semibold'
					: 'border-transparent text-muted-foreground hover:text-foreground hover:border-border/50'}"
				aria-current={isActive ? 'page' : undefined}
			>
				{#if tab.icon}
					{@const Icon = tab.icon}
					<Icon class="w-4 h-4" />
				{/if}
				{tab.label}
			</a>
		{/each}
	</nav>
</div>
