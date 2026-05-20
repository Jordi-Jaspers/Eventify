<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import Button from '$lib/components/ui/button/button.svelte';
	import {
		getAdminCounts,
		getAdminGrowth,
		getEventVolume,
		getStorageStats,
		getEventStats
	} from '$lib/api/admin/AdminController';
	import { getAdminApiKeyStats } from '$lib/api/admin/AdminApiKeyController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { CircleAlert } from '@lucide/svelte';
	import type {
		AdminApiKeyStatsResponse,
		TableSizeEntry,
		AdminCountsResponse,
		AdminGrowthResponse,
		AdminEventVolumeResponse,
		AdminEventStatsResponse
	} from '$lib/api/models.ts';
	import OverviewTab from '$lib/components/admin/statistics/OverviewTab.svelte';
	import InfrastructureTab from '$lib/components/admin/statistics/InfrastructureTab.svelte';
	import EventsTab from '$lib/components/admin/statistics/EventsTab.svelte';

	// ── State ──────────────────────────────────────────────────────────────────
	let counts: AdminCountsResponse | null = $state(null);
	let growth: AdminGrowthResponse | null = $state(null);
	let eventVolume: AdminEventVolumeResponse | null = $state(null);
	let apiKeyStats: AdminApiKeyStatsResponse | null = $state(null);
	let storageStats: TableSizeEntry[] = $state([]);
	let eventStats: AdminEventStatsResponse | null = $state(null);

	// Per-section loading states
	let countsLoading: boolean = $state(false);
	let growthLoading: boolean = $state(false);
	let infraLoading: boolean = $state(false);
	let eventsLoading: boolean = $state(false);
	let volumeLoading: boolean = $state(false);

	// Per-section error states
	let overviewError: string | null = $state(null);
	let infraError: string | null = $state(null);
	let eventsError: string | null = $state(null);

	// ── URL params ─────────────────────────────────────────────────────────────
	type Tab = 'overview' | 'infrastructure' | 'events';
	type Days = 7 | 30 | 90 | 180;

	const activeTab: Tab = $derived(
		((): Tab => {
			const t: string | null = $page.url.searchParams.get('tab');
			if (t === 'infrastructure') return 'infrastructure';
			if (t === 'events') return 'events';
			return 'overview';
		})()
	);
	const selectedDays: Days = $derived(
		((): Days => {
			const d: number = Number($page.url.searchParams.get('days') ?? '30');
			if (d === 7 || d === 90 || d === 180) return d as Days;
			return 30;
		})()
	);

	function setTab(tab: Tab): void {
		const url: URL = new URL($page.url);
		url.searchParams.set('tab', tab);
		goto(url.toString(), { replaceState: true });
		if (tab === 'infrastructure' && !apiKeyStats) {
			loadInfra();
		}
		if (tab === 'events' && !eventStats) {
			loadEvents();
		}
	}

	function setDays(days: Days): void {
		const url: URL = new URL($page.url);
		url.searchParams.set('days', String(days));
		goto(url.toString(), { replaceState: true });
		loadOverview(days);
		if (activeTab === 'events') {
			loadEvents(days);
		}
	}

	// ── Data loading ───────────────────────────────────────────────────────────
	async function loadOverview(days?: Days): Promise<void> {
		overviewError = null;
		countsLoading = true;
		growthLoading = true;
		try {
			const [c, g, v]: [AdminCountsResponse, AdminGrowthResponse, AdminEventVolumeResponse] =
				await Promise.all([
					getAdminCounts().finally(() => (countsLoading = false)),
					getAdminGrowth(days ?? selectedDays).finally(() => (growthLoading = false)),
					getEventVolume(days ?? selectedDays).finally(() => (growthLoading = false))
				]);
			counts = c;
			growth = g;
			eventVolume = v;
		} catch (err: unknown) {
			countsLoading = false;
			growthLoading = false;
			const { message }: { message: string } = handleError(err, 'Failed to load overview statistics');
			overviewError = message;
			toast.error(message);
		}
	}

	async function loadInfra(): Promise<void> {
		infraError = null;
		infraLoading = true;
		try {
			const [keys, storage, c]: [AdminApiKeyStatsResponse, TableSizeEntry[], AdminCountsResponse] =
				await Promise.all([
					getAdminApiKeyStats(),
					getStorageStats(),
					counts ? Promise.resolve(counts) : getAdminCounts()
				]);
			apiKeyStats = keys;
			storageStats = storage;
			counts = c;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load infrastructure stats');
			infraError = message;
			toast.error(message);
		} finally {
			infraLoading = false;
		}
	}

	async function loadEvents(days?: Days): Promise<void> {
		eventsError = null;
		eventsLoading = true;
		volumeLoading = true;
		try {
			const [es, ev]: [AdminEventStatsResponse, AdminEventVolumeResponse] = await Promise.all([
				getEventStats(days ?? selectedDays).finally(() => (eventsLoading = false)),
				getEventVolume(days ?? selectedDays).finally(() => (volumeLoading = false))
			]);
			eventStats = es;
			eventVolume = ev;
		} catch (err: unknown) {
			eventsLoading = false;
			volumeLoading = false;
			const { message }: { message: string } = handleError(err, 'Failed to load event stats');
			eventsError = message;
			toast.error(message);
		}
	}

	onMount((): void => {
		loadOverview();
		if (activeTab === 'infrastructure') {
			loadInfra();
		}
		if (activeTab === 'events') {
			loadEvents();
		}
	});
</script>

{#snippet pillToggle(items: { value: string; label: string }[], active: string, onSelect: (v: string) => void, size?: 'sm' | 'md')}
	{@const px = size === 'sm' ? 'px-3 py-1' : 'px-4 py-1.5'}
	{@const text = size === 'sm' ? 'text-xs' : 'text-sm'}
	<div class="flex items-center gap-1 bg-muted/40 rounded-full p-1 border border-border/50">
		{#each items as item (item.value)}
			<button
				class="{px} rounded-full {text} font-medium transition-all {active === item.value
					? 'bg-primary text-primary-foreground shadow-sm'
					: 'text-muted-foreground hover:text-foreground'}"
				onclick={() => onSelect(item.value)}
			>
				{item.label}
			</button>
		{/each}
	</div>
{/snippet}

{#snippet loadingSkeleton(rows: number, height?: string)}
	{@const h = height ?? 'h-6'}
	<div class="space-y-3">
		{#each Array(rows) as _, i (i)}
			<div class="{h} bg-muted animate-pulse rounded"></div>
		{/each}
	</div>
{/snippet}

{#snippet sectionError(message: string, onRetry: () => void)}
	<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
		<CircleAlert class="h-4 w-4" />
		<AlertDescription>
			{message}
			<Button variant="outline" size="sm" class="ml-4" onclick={onRetry}>Retry</Button>
		</AlertDescription>
	</Alert>
{/snippet}

<svelte:head>
	<title>Admin Dashboard - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">

		<!-- Header -->
		<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-2">
			<div>
				<h1 class="text-3xl font-bold text-primary">Admin Dashboard</h1>
				<p class="text-muted-foreground mt-1">Platform statistics and infrastructure overview</p>
			</div>
		</div>

		<!-- Tab navigation + Time range -->
		<div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
			{@render pillToggle(
				[
					{ value: 'overview', label: 'Overview' },
					{ value: 'infrastructure', label: 'Infrastructure' },
					{ value: 'events', label: 'Events' }
				],
				activeTab,
				(v) => setTab(v as Tab),
				'md'
			)}
			{@render pillToggle(
				([7, 30, 90, 180] as Days[]).map((d) => ({ value: String(d), label: `${d}d` })),
				String(selectedDays),
				(v) => setDays(Number(v) as Days),
				'sm'
			)}
		</div>

		{#if activeTab === 'overview'}
			{#if overviewError}
				{@render sectionError(overviewError, () => loadOverview())}
			{:else}
				<OverviewTab
					{counts}
					{growth}
					{eventVolume}
					{countsLoading}
					{growthLoading}
					{loadingSkeleton}
				/>
			{/if}
		{:else if activeTab === 'infrastructure'}
			{#if infraError}
				{@render sectionError(infraError, () => loadInfra())}
			{:else}
				<InfrastructureTab
					{counts}
					{apiKeyStats}
					{storageStats}
					countsLoading={countsLoading || infraLoading}
					{infraLoading}
					{loadingSkeleton}
				/>
			{/if}
		{:else if activeTab === 'events'}
			{#if eventsError}
				{@render sectionError(eventsError, () => loadEvents())}
			{:else}
				<EventsTab
					{eventStats}
					{eventVolume}
					{selectedDays}
					{eventsLoading}
					{volumeLoading}
					{loadingSkeleton}
				/>
			{/if}
		{/if}

	</div>
</main>
