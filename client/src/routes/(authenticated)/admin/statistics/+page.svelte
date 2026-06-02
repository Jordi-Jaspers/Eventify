<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/state';
	import {
		getAdminCounts,
		getAdminGrowth,
		getEventVolume,
		getStorageStats,
		getEventStats
	} from '$lib/api/admin/AdminController';
	import type { AdminStatsRequest } from '$lib/api/models.ts';
	import { getAdminApiKeyStats } from '$lib/api/admin/AdminApiKeyController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { TimeRangePopover } from '$lib/components/ui/time-range-popover';
	import { ErrorAlert } from '$lib/components/ui/error-alert';
	import { computeDaysFromRange } from '$lib/utils/time-range';
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

	const activeTab: Tab = $derived(
		page.url.searchParams.get('tab') === 'infrastructure' ? 'infrastructure'
		: page.url.searchParams.get('tab') === 'events' ? 'events'
		: 'overview'
	);

	let selectedDays: string = $state(page.url.searchParams.get('days') ?? '30');
	let isCustomRange: boolean = $state(
		page.url.searchParams.has('start') && page.url.searchParams.has('end')
	);
	let customStart: string = $state(page.url.searchParams.get('start') ?? '');
	let customEnd: string = $state(page.url.searchParams.get('end') ?? '');

	function buildStatsRequest(): AdminStatsRequest {
		if (isCustomRange && customStart && customEnd) {
			return { startDate: customStart, endDate: customEnd };
		}
		return { days: Number(selectedDays) };
	}

	function buildDaysForEventTab(): number {
		return computeDaysFromRange(Number(selectedDays), isCustomRange, customStart, customEnd);
	}

	function setTab(tab: Tab): void {
		const url: URL = new URL(page.url);
		url.searchParams.set('tab', tab);
		goto(url.toString(), { replaceState: true });
		if (tab === 'infrastructure' && !apiKeyStats) {
			loadInfra();
		}
		if (tab === 'events' && !eventStats) {
			loadEvents();
		}
	}

	function reloadAfterRangeChange(): void {
		loadOverview();
		if (activeTab === 'events') loadEvents();
	}

	function handleQuickRangeSelect(days: string): void {
		selectedDays = days;
		isCustomRange = false;
		customStart = '';
		customEnd = '';
		const url: URL = new URL(page.url);
		url.searchParams.set('days', days);
		url.searchParams.delete('start');
		url.searchParams.delete('end');
		goto(url.toString(), { replaceState: true });
		reloadAfterRangeChange();
	}

	function handleCustomRangeApply(start: string, end: string): void {
		customStart = start;
		customEnd = end;
		isCustomRange = true;
		const url: URL = new URL(page.url);
		url.searchParams.delete('days');
		url.searchParams.set('start', start);
		url.searchParams.set('end', end);
		goto(url.toString(), { replaceState: true });
		reloadAfterRangeChange();
	}

	function handleCustomRangeToggle(): void {
		isCustomRange = true;
	}

	// ── Data loading ───────────────────────────────────────────────────────────
	function showError(err: unknown, fallback: string, setter: (msg: string) => void): void {
		const { message } = handleError(err, fallback);
		setter(message);
		toast.error(message);
	}

	async function loadOverview(): Promise<void> {
		overviewError = null;
		countsLoading = true;
		growthLoading = true;
		const request: AdminStatsRequest = buildStatsRequest();
		try {
			const [c, g, v] = await Promise.all([
				getAdminCounts().finally(() => (countsLoading = false)),
				getAdminGrowth(request).finally(() => (growthLoading = false)),
				getEventVolume(request).finally(() => (growthLoading = false))
			]);
			counts = c;
			growth = g;
			eventVolume = v;
		} catch (err: unknown) {
			countsLoading = false;
			growthLoading = false;
			showError(err, 'Failed to load overview statistics', (m) => (overviewError = m));
		}
	}

	async function loadInfra(): Promise<void> {
		infraError = null;
		infraLoading = true;
		try {
			const [keys, storage, c] = await Promise.all([
				getAdminApiKeyStats(),
				getStorageStats(),
				counts ? Promise.resolve(counts) : getAdminCounts()
			]);
			apiKeyStats = keys;
			storageStats = storage;
			counts = c;
		} catch (err: unknown) {
			showError(err, 'Failed to load infrastructure stats', (m) => (infraError = m));
		} finally {
			infraLoading = false;
		}
	}

	async function loadEvents(): Promise<void> {
		eventsError = null;
		eventsLoading = true;
		volumeLoading = true;
		const request: AdminStatsRequest = buildStatsRequest();
		try {
			const [es, ev] = await Promise.all([
				getEventStats(request).finally(() => (eventsLoading = false)),
				getEventVolume(request).finally(() => (volumeLoading = false))
			]);
			eventStats = es;
			eventVolume = ev;
		} catch (err: unknown) {
			eventsLoading = false;
			volumeLoading = false;
			showError(err, 'Failed to load event stats', (m) => (eventsError = m));
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

{#snippet loadingSkeleton(rows: number, height?: string)}
	{@const h = height ?? 'h-6'}
	<div class="space-y-3">
		{#each Array(rows) as _, i (i)}
			<div class="{h} bg-muted animate-pulse rounded"></div>
		{/each}
	</div>
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
			<div class="border-b border-border/50">
				<nav class="flex gap-1 px-1">
					{#each [{ value: 'overview', label: 'Overview' }, { value: 'infrastructure', label: 'Infrastructure' }, { value: 'events', label: 'Events' }] as tab (tab.value)}
						<button
							onclick={() => setTab(tab.value as Tab)}
							class="px-4 py-2 text-sm font-medium transition-colors border-b-2 -mb-px
								{activeTab === tab.value
								? 'border-primary text-primary'
								: 'border-transparent text-muted-foreground hover:text-foreground hover:border-border'}"
						>
							{tab.label}
						</button>
					{/each}
				</nav>
			</div>
			<TimeRangePopover
				{selectedDays}
				{isCustomRange}
				{customStart}
				{customEnd}
				onQuickRangeSelect={handleQuickRangeSelect}
				onCustomRangeApply={handleCustomRangeApply}
				onCustomRangeToggle={handleCustomRangeToggle}
			/>
		</div>

		{#if activeTab === 'overview'}
			{#if overviewError}
				<ErrorAlert message={overviewError} onRetry={() => loadOverview()} />
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
				<ErrorAlert message={infraError} onRetry={() => loadInfra()} />
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
				<ErrorAlert message={eventsError} onRetry={() => loadEvents()} />
			{:else}
				<EventsTab
					{eventStats}
					{eventVolume}
					selectedDays={buildDaysForEventTab()}
					{eventsLoading}
					{volumeLoading}
					{loadingSkeleton}
				/>
			{/if}
		{/if}

	</div>
</main>
