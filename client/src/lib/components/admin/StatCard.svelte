<script lang="ts">
	import type { Component } from 'svelte';
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';

	interface Props {
		title: string;
		value: string | number;
		icon: Component;
		loading?: boolean;
		variant?: 'primary' | 'blue' | 'purple' | 'green' | 'yellow' | 'orange' | 'red' | 'accent';
		subtitle?: string;
	}

	const {
		title,
		value,
		icon: Icon,
		loading = false,
		variant = 'primary',
		subtitle
	}: Props = $props();

	const variantClasses: Record<string, { border: string; gradient: string; text: string }> = {
		primary: {
			border: 'hover:shadow-primary/20 hover:border-primary/50',
			gradient: 'from-primary/10 via-transparent to-primary/5',
			text: 'text-primary bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent'
		},
		blue: {
			border: 'hover:shadow-blue-500/20 hover:border-blue-500/50',
			gradient: 'from-blue-500/10 via-transparent to-blue-500/5',
			text: 'text-blue-500 bg-gradient-to-r from-blue-500 to-blue-400 bg-clip-text text-transparent'
		},
		purple: {
			border: 'hover:shadow-purple-500/20 hover:border-purple-500/50',
			gradient: 'from-purple-500/10 via-transparent to-purple-500/5',
			text: 'text-purple-500 bg-gradient-to-r from-purple-500 to-purple-400 bg-clip-text text-transparent'
		},
		green: {
			border: 'hover:shadow-green-500/20 hover:border-green-500/50',
			gradient: 'from-green-500/10 via-transparent to-green-500/5',
			text: 'text-green-500 bg-gradient-to-r from-green-500 to-green-400 bg-clip-text text-transparent'
		},
		yellow: {
			border: 'hover:shadow-yellow-500/20 hover:border-yellow-500/50',
			gradient: 'from-yellow-500/10 via-transparent to-yellow-500/5',
			text: 'text-yellow-500 bg-gradient-to-r from-yellow-500 to-yellow-400 bg-clip-text text-transparent'
		},
		orange: {
			border: 'border-orange-500/30 hover:shadow-orange-500/20 hover:border-orange-500/50',
			gradient: 'from-orange-500/10 via-transparent to-orange-500/5',
			text: 'text-orange-500 bg-gradient-to-r from-orange-500 to-orange-400 bg-clip-text text-transparent'
		},
		red: {
			border: 'hover:shadow-red-500/20 hover:border-red-500/50',
			gradient: 'from-red-500/10 via-transparent to-red-500/5',
			text: 'text-red-500 bg-gradient-to-r from-red-500 to-red-400 bg-clip-text text-transparent'
		},
		accent: {
			border: 'hover:shadow-accent/20 hover:border-accent/50',
			gradient: 'from-accent/10 via-transparent to-accent/5',
			text: 'text-accent bg-gradient-to-r from-accent to-primary bg-clip-text text-transparent'
		}
	};

	const classes: { border: string; gradient: string; text: string } = $derived(variantClasses[variant]);
</script>

<Card
	class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl relative overflow-hidden {classes.border} transition-all duration-300"
>
	<div class="absolute inset-0 bg-gradient-to-br {classes.gradient} opacity-50"></div>
	<CardHeader class="relative z-10">
		<div class="flex items-center justify-between">
			<CardTitle class="text-sm font-medium text-muted-foreground">{title}</CardTitle>
			<Icon class="h-5 w-5 {classes.text.split(' ')[0]}" />
		</div>
	</CardHeader>
	<CardContent class="relative z-10">
		{#if loading}
			<div class="h-10 bg-muted/50 rounded animate-pulse"></div>
		{:else}
			<div class="text-3xl font-bold {classes.text}">
				{value}
			</div>
			{#if subtitle}
				<div class="text-xs text-muted-foreground truncate mt-1">
					{subtitle}
				</div>
			{/if}
		{/if}
	</CardContent>
</Card>
