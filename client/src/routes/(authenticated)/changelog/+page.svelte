<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Sparkles, Calendar } from '@lucide/svelte';
	import type { ChangelogEntry } from '$lib/types/changelog';
	import { formatDate } from '$lib/utils/date';
	import type { PageData } from './$types';

	interface Props {
		data: PageData;
	}

	let { data }: Props = $props();

	interface CategoryConfig {
		key: keyof Pick<ChangelogEntry, 'features' | 'improvements' | 'fixes'>;
		title: string;
		badgeClass: string;
		bulletClass: string;
	}

	const categories: CategoryConfig[] = [
		{
			key: 'features',
			title: 'New',
			badgeClass: 'bg-green-500/20 text-green-400 border-green-500/30',
			bulletClass: 'text-green-400'
		},
		{
			key: 'improvements',
			title: 'Improved',
			badgeClass: 'bg-blue-500/20 text-blue-400 border-blue-500/30',
			bulletClass: 'text-blue-400'
		},
		{
			key: 'fixes',
			title: 'Fixed',
			badgeClass: 'bg-amber-500/20 text-amber-400 border-amber-500/30',
			bulletClass: 'text-amber-400'
		}
	];
</script>

<svelte:head>
	<title>What's New - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-primary flex items-center gap-2">
				<Sparkles class="w-7 h-7" />
				What's New in Eventify
			</h1>
			<p class="text-muted-foreground mt-2">
				Stay up to date with the latest features, improvements, and bug fixes
			</p>
		</div>

		<div class="space-y-6">
			{#each data.changelog as entry (entry.version)}
				<Card class="border-border/50 bg-card/50 backdrop-blur-sm">
					<CardHeader>
						<div class="flex items-center justify-between flex-wrap gap-2">
							<CardTitle class="text-xl flex items-center gap-2">
								<span class="font-mono text-primary">{entry.version}</span>
							</CardTitle>
							<div class="flex items-center gap-2 text-sm text-muted-foreground">
								<Calendar class="w-4 h-4" />
								<span>{formatDate(entry.date)}</span>
							</div>
						</div>
					</CardHeader>
					<CardContent class="space-y-6">
						{#each categories as category (category.key)}
							{@const items = entry[category.key]}
							{#if items && items.length > 0}
								<div class="space-y-3">
									<Badge class="{category.badgeClass} border font-medium" variant="outline">
										{category.title}
									</Badge>
									<ul class="space-y-2 ml-4">
										{#each items as item}
											<li class="text-sm text-foreground flex items-start gap-2">
												<span class="{category.bulletClass} mt-0.5">•</span>
												<span>{item}</span>
											</li>
										{/each}
									</ul>
								</div>
							{/if}
						{/each}
					</CardContent>
				</Card>
			{/each}
		</div>
	</div>
</main>
