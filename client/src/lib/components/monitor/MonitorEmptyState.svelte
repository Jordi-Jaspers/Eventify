<script lang="ts">
	import type { Component } from 'svelte';
	import { Card, CardContent } from '$lib/components/ui/card';
	import { Button } from '$lib/components/ui/button';

	interface Props {
		icon: Component;
		iconVariant?: 'primary' | 'destructive';
		title: string;
		description: string;
		actionLabel?: string;
		onAction?: () => void;
	}

	const { icon: Icon, iconVariant = 'primary', title, description, actionLabel, onAction }: Props = $props();

	const iconBgClass = $derived(iconVariant === 'destructive' ? 'bg-destructive/10' : 'bg-primary/10');
	const iconTextClass = $derived(iconVariant === 'destructive' ? 'text-destructive' : 'text-primary');
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-lg">
	<CardContent class="p-12">
		<div class="flex flex-col items-center justify-center text-center">
			<div class="h-20 w-20 rounded-full {iconBgClass} flex items-center justify-center mb-6">
				<Icon class="h-10 w-10 {iconTextClass}" />
			</div>
			<h3 class="text-xl font-semibold mb-2">{title}</h3>
			<p class="text-muted-foreground max-w-md mb-6">
				{description}
			</p>
			{#if actionLabel && onAction}
				<Button onclick={onAction} variant="outline">
					{actionLabel}
				</Button>
			{/if}
		</div>
	</CardContent>
</Card>
