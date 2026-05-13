<script lang="ts">
	import { Card, CardContent, CardHeader, CardTitle } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Sparkles, Calendar } from '@lucide/svelte';
	import { changelog } from '$lib/data/changelog';
	import type { ChangelogEntry } from '$lib/types/changelog';
	import { formatDate } from '$lib/utils/date';

	interface CategoryConfig {
		title: string;
		badgeClass: string;
	}

	const categoryConfig: Record<string, CategoryConfig> = {
		features: {
			title: 'New',
			badgeClass: 'bg-green-500/20 text-green-400 border-green-500/30'
		},
		improvements: {
			title: 'Improved',
			badgeClass: 'bg-blue-500/20 text-blue-400 border-blue-500/30'
		},
		fixes: {
			title: 'Fixed',
			badgeClass: 'bg-amber-500/20 text-amber-400 border-amber-500/30'
		}
	};
</script>

<svelte:head>
	<title>What's New - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-4xl mx-auto space-y-6 animate-fade-in">
		<!-- Page Header -->
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-primary flex items-center gap-2">
				<Sparkles class="w-7 h-7" />
				What's New in Eventify
			</h1>
			<p class="text-muted-foreground mt-2">
				Stay up to date with the latest features, improvements, and bug fixes
			</p>
		</div>

		<!-- Changelog Entries -->
		<div class="space-y-6">
			{#each changelog as entry (entry.version)}
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
						<!-- Features Section -->
						{#if entry.features && entry.features.length > 0}
							<div class="space-y-3">
								<Badge
									class="{categoryConfig.features.badgeClass} border font-medium"
									variant="outline"
								>
									{categoryConfig.features.title}
								</Badge>
								<ul class="space-y-2 ml-4">
									{#each entry.features as feature}
										<li class="text-sm text-foreground flex items-start gap-2">
											<span class="text-green-400 mt-0.5">•</span>
											<span>{feature}</span>
										</li>
									{/each}
								</ul>
							</div>
						{/if}

						<!-- Improvements Section -->
						{#if entry.improvements && entry.improvements.length > 0}
							<div class="space-y-3">
								<Badge
									class="{categoryConfig.improvements.badgeClass} border font-medium"
									variant="outline"
								>
									{categoryConfig.improvements.title}
								</Badge>
								<ul class="space-y-2 ml-4">
									{#each entry.improvements as improvement}
										<li class="text-sm text-foreground flex items-start gap-2">
											<span class="text-blue-400 mt-0.5">•</span>
											<span>{improvement}</span>
										</li>
									{/each}
								</ul>
							</div>
						{/if}

						<!-- Fixes Section -->
						{#if entry.fixes && entry.fixes.length > 0}
							<div class="space-y-3">
								<Badge
									class="{categoryConfig.fixes.badgeClass} border font-medium"
									variant="outline"
								>
									{categoryConfig.fixes.title}
								</Badge>
								<ul class="space-y-2 ml-4">
									{#each entry.fixes as fix}
										<li class="text-sm text-foreground flex items-start gap-2">
											<span class="text-amber-400 mt-0.5">•</span>
											<span>{fix}</span>
										</li>
									{/each}
								</ul>
							</div>
						{/if}
					</CardContent>
				</Card>
			{/each}
		</div>
	</div>
</main>
