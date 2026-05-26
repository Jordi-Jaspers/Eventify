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
	import { getAdminApiKeyStats } from '$lib/api/admin/AdminApiKeyController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { PillToggle } from '$lib/components/ui/pill-toggle';
	import { ErrorAlert } from '$lib/components/ui/error-alert';
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
			const t: string | null = page.url.searchParams.get('tab');
			if (t === 'infrastructure') return 'infrastructure';
			if (t === 'events') return 'events';
			return 'overview';
		})()
	);
	const selectedDays: Days = $derived(
		((): Days => {
			const d: number = Number(page.url.searchParams.get('days') ?? '30');
			if (d === 7 || d === 90 || d === 180) return d as Days;
			return 30;
		})()
	);

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

	function setDays(days: Days): void {
		const url: URL = new URL(page.url);
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
			<PillToggle
				items={[
					{ value: 'overview', label: 'Overview' },
					{ value: 'infrastructure', label: 'Infrastructure' },
					{ value: 'events', label: 'Events' }
				]}
				active={activeTab}
				onSelect={(v) => setTab(v as Tab)}
				size="md"
			/>
			<PillToggle
				items={([7, 30, 90, 180] as Days[]).map((d) => ({ value: String(d), label: `${d}d` }))}
				active={String(selectedDays)}
				onSelect={(v) => setDays(Number(v) as Days)}
				size="sm"
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
					{selectedDays}
					{eventsLoading}
					{volumeLoading}
					{loadingSkeleton}
				/>
			{/if}
		{/if}

	</div>
</main>
