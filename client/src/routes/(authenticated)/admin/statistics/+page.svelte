<script lang="ts">
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { Alert, AlertDescription } from '$lib/components/ui/alert';
	import Button from '$lib/components/ui/button/button.svelte';
	import { getAdminStats, getStorageStats, getEventStats } from '$lib/api/admin/AdminController';
	import { getAdminApiKeyStats } from '$lib/api/admin/AdminApiKeyController';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import { CircleAlert } from '@lucide/svelte';
	import type {
		AdminStatsResponse,
		AdminApiKeyStatsResponse,
		TableSizeEntry
	} from '$lib/api/models.ts';
	import type { AdminEventStatsResponse } from '$lib/api/admin/AdminController';
	import OverviewTab from '$lib/components/admin/statistics/OverviewTab.svelte';
	import InfrastructureTab from '$lib/components/admin/statistics/InfrastructureTab.svelte';
	import EventsTab from '$lib/components/admin/statistics/EventsTab.svelte';

	// ── State ──────────────────────────────────────────────────────────────────
	let stats: AdminStatsResponse | null = $state(null);
	let apiKeyStats: AdminApiKeyStatsResponse | null = $state(null);
	let storageStats: TableSizeEntry[] = $state([]);
	let eventStats: AdminEventStatsResponse | null = $state(null);
	let loading: boolean = $state(true);
	let infraLoading: boolean = $state(false);
	let eventsLoading: boolean = $state(false);
	let error: string | null = $state(null);

	// ── URL params ─────────────────────────────────────────────────────────────
	type Tab = 'overview' | 'infrastructure' | 'events';
	type Days = 7 | 30 | 90;

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
			return (d === 7 || d === 90 ? d : 30) as Days;
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
		loadStats(days);
		if (activeTab === 'events') {
			loadEvents(days);
		}
	}

	// ── Data loading ───────────────────────────────────────────────────────────
	async function loadStats(days?: Days): Promise<void> {
		loading = true;
		error = null;
		try {
			stats = await getAdminStats(days ?? selectedDays);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load admin statistics');
			error = message;
			toast.error(message);
		} finally {
			loading = false;
		}
	}

	async function loadInfra(): Promise<void> {
		infraLoading = true;
		try {
			const [keys, storage]: [AdminApiKeyStatsResponse, TableSizeEntry[]] = await Promise.all([
				getAdminApiKeyStats(),
				getStorageStats()
			]);
			apiKeyStats = keys;
			storageStats = storage;
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load infrastructure stats');
			toast.error(message);
		} finally {
			infraLoading = false;
		}
	}

	async function loadEvents(days?: Days): Promise<void> {
		eventsLoading = true;
		try {
			eventStats = await getEventStats(days ?? selectedDays);
		} catch (err: unknown) {
			const { message }: { message: string } = handleError(err, 'Failed to load event stats');
			toast.error(message);
		} finally {
			eventsLoading = false;
		}
	}

	onMount((): void => {
		loadStats();
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
				([7, 30, 90] as Days[]).map((d) => ({ value: String(d), label: `${d}d` })),
				String(selectedDays),
				(v) => setDays(Number(v) as Days),
				'sm'
			)}
		</div>

		<!-- Error Alert -->
		{#if error && !loading}
			<Alert variant="destructive" class="bg-destructive/10 border-destructive/50 backdrop-blur-sm">
				<CircleAlert class="h-4 w-4" />
				<AlertDescription>
					{error}
					<Button variant="outline" size="sm" class="ml-4" onclick={() => loadStats()}>Retry</Button>
				</AlertDescription>
			</Alert>
		{/if}

		{#if activeTab === 'overview'}
			<OverviewTab {stats} {loading} {loadingSkeleton} />
		{:else if activeTab === 'infrastructure'}
			<InfrastructureTab {stats} {apiKeyStats} {storageStats} {loading} {infraLoading} {loadingSkeleton} />
		{:else if activeTab === 'events'}
			<EventsTab {eventStats} {selectedDays} {eventsLoading} {loadingSkeleton} />
		{/if}

	</div>
</main>
