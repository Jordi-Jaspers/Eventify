<script lang="ts">
	import { onMount } from 'svelte';
	import { DataTable, createDataTableService } from '$lib/components/data-table';
	import type { DataTableColumn } from '$lib/components/data-table/types';
	import { searchAuditLog, getAuditLogStats } from '$lib/api/admin/AdminAuditLogController';
	import type { AuditLogResponse, AuditLogStatsResponse } from '$lib/api/models';
	import { Badge } from '$lib/components/ui/badge';
	import { Button } from '$lib/components/ui/button';
	import { ScrollText } from '@lucide/svelte';
	import { formatDateTime } from '$lib/utils/date';
	import { handleError } from '$lib/utils/error-handler';
	import { toast } from 'svelte-sonner';
	import AuditLogKpiCards from './AuditLogKpiCards.svelte';
	import AuditLogSparkline from './AuditLogSparkline.svelte';

	const columns: DataTableColumn<AuditLogResponse>[] = [
		{
			key: 'actor',
			label: 'Actor',
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search by name or email...',
			colSpan: 2
		},
		{
			key: 'method',
			label: 'Method',
			sortable: true,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ label: 'GET', value: 'GET' },
				{ label: 'POST', value: 'POST' },
				{ label: 'PUT', value: 'PUT' },
				{ label: 'PATCH', value: 'PATCH' },
				{ label: 'DELETE', value: 'DELETE' }
			],
			colSpan: 1
		},
		{
			key: 'path',
			label: 'Path',
			sortable: true,
			filterable: true,
			filterType: 'FUZZY_TEXT',
			filterPlaceholder: 'Search path...',
			colSpan: 3
		},
		{
			key: 'excludePath',
			label: 'Exclude Path',
			filterable: true,
			filterType: 'TEXT',
			filterPlaceholder: 'e.g. /health,/auth/refresh',
			colSpan: 0
		},
		{
			key: 'status',
			label: 'Status',
			sortable: false,
			filterable: true,
			filterType: 'MULTI_ENUM',
			filterOptions: [
				{ label: '2xx Success', value: '2xx' },
				{ label: '4xx Client Error', value: '4xx' },
				{ label: '5xx Server Error', value: '5xx' }
			],
			colSpan: 1
		},
		{
			key: 'ipAddress',
			label: 'IP Address',
			sortable: true,
			colSpan: 2
		},
		{
			key: 'createdAt',
			label: 'Timestamp',
			sortable: true,
			filterable: true,
			filterType: 'DATE',
			colSpan: 2
		}
	];

	const dataTableService = createDataTableService<AuditLogResponse>({
		fetchFn: searchAuditLog,
		pageSize: 20,
		defaultSort: [{ name: 'createdAt', direction: 'DESC' }]
	});

	let expandedId: number | null = $state(null);

	let stats: AuditLogStatsResponse | null = $state(null);
	let statsLoading: boolean = $state(true);

	const NOISE_PATHS: string = '/v1/health,/v1/auth/refresh';

	function addNoisePreset(): void {
		const current = (dataTableService.filters['excludePath'] as string) ?? '';
		const existing = current ? current.split(',').map((s: string) => s.trim()) : [];
		const toAdd = NOISE_PATHS.split(',').filter((p: string) => !existing.includes(p));
		const merged = [...existing, ...toAdd].join(',');
		dataTableService.setFilter('excludePath', merged);
	}

	function getMethodBadgeClass(method: string): string {
		switch (method) {
			case 'GET': return 'bg-blue-500/20 text-blue-400 border-blue-500/30';
			case 'POST': return 'bg-green-500/20 text-green-400 border-green-500/30';
			case 'PUT': return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
			case 'PATCH': return 'bg-orange-500/20 text-orange-400 border-orange-500/30';
			case 'DELETE': return 'bg-red-500/20 text-red-400 border-red-500/30';
			default: return 'bg-muted text-muted-foreground';
		}
	}

	function getStatusBadgeClass(statusCode: number): string {
		if (statusCode >= 200 && statusCode < 300) return 'bg-green-500/20 text-green-400 border-green-500/30';
		if (statusCode >= 400 && statusCode < 500) return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
		if (statusCode >= 500) return 'bg-red-500/20 text-red-400 border-red-500/30';
		return 'bg-muted text-muted-foreground';
	}

	function formatRequestBody(body: string | null | undefined): string {
		if (!body) return 'No request body';
		try {
			return JSON.stringify(JSON.parse(body), null, 2);
		} catch {
			return body;
		}
	}

	function toggleExpand(id: number): void {
		expandedId = expandedId === id ? null : id;
	}

	async function loadStats(from: string, to: string): Promise<void> {
		statsLoading = true;
		try {
			stats = await getAuditLogStats(from, to);
		} catch (err: unknown) {
			const { message } = handleError(err, 'Failed to load stats');
			toast.error(message);
		} finally {
			statsLoading = false;
		}
	}

	function activateErrorFilter(): void {
		dataTableService.setFilter('status', ['4xx', '5xx']);
		dataTableService.clearFilter('method');
	}

	function activateMutationFilter(): void {
		dataTableService.setFilter('method', ['POST', 'PUT', 'PATCH', 'DELETE']);
		dataTableService.clearFilter('status');
	}

	function handleHourClick(from: string, to: string): void {
		dataTableService.setFilter('createdAt', { from, to });
	}

	// Reload stats when createdAt filter changes
	const currentDateFilter = $derived(dataTableService.filters['createdAt'] as { from?: string; to?: string } | null);

	$effect(() => {
		if (currentDateFilter?.from && currentDateFilter?.to) {
			loadStats(currentDateFilter.from, currentDateFilter.to);
		}
	});

	onMount(() => {
		const today: Date = new Date();
		const todayStart: string = new Date(today.getFullYear(), today.getMonth(), today.getDate()).toISOString();
		const todayEnd: string = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59).toISOString();
		dataTableService.setFilter('createdAt', { from: todayStart, to: todayEnd });
	});

	const errorRate: string = $derived.by((): string => {
		if (!stats || stats.totalRequests === 0) return '0.0';
		return ((stats.errorCount / stats.totalRequests) * 100).toFixed(1);
	});

	onMount(() => {
		const today: Date = new Date();
		const todayStart: string = new Date(today.getFullYear(), today.getMonth(), today.getDate()).toISOString();
		const todayEnd: string = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 23, 59, 59).toISOString();
		dataTableService.setFilter('createdAt', { from: todayStart, to: todayEnd });
	});
</script>

<svelte:head>
	<title>Audit Log - Admin - Eventify</title>
</svelte:head>

<main class="container mx-auto px-4 py-8">
	<div class="max-w-7xl mx-auto space-y-6 animate-fade-in">
		<!-- Header -->
		<div class="mb-8">
			<h1 class="text-3xl font-bold text-primary">Audit Log</h1>
			<p class="text-muted-foreground mt-2">Track all API activity and requests on the platform</p>
		</div>

		<!-- KPI Cards -->
		<AuditLogKpiCards
			{stats}
			{statsLoading}
			{errorRate}
			onErrorClick={activateErrorFilter}
			onMutationClick={activateMutationFilter}
		/>

		<!-- Sparkline Timeline -->
		<AuditLogSparkline {stats} {statsLoading} onHourClick={handleHourClick} />

		<!-- DataTable -->
		<DataTable {columns} service={dataTableService} title="Audit Log" icon={ScrollText}>
			{#snippet headerActions()}
				<Button variant="outline" size="sm" class="h-8 text-xs" onclick={addNoisePreset}>
					Hide noise
				</Button>
			{/snippet}
			{#snippet row(item: AuditLogResponse)}
				<div class="w-full">
					<div
						role="button"
						tabindex="0"
						class="hidden md:grid items-center gap-4 p-4 hover:bg-muted/30 transition-all text-left w-full cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
						style="grid-template-columns: repeat(11, minmax(0, 1fr));"
						onclick={() => toggleExpand(item.id)}
						onkeydown={(e: KeyboardEvent) => { if (e.key === 'Enter' || e.key === ' ') toggleExpand(item.id); }}
					>
						<!-- Actor -->
						<div style="grid-column: span 2;" class="flex items-center">
							<span class="text-sm truncate">{item.actorEmail}</span>
						</div>

						<!-- Method -->
						<div style="grid-column: span 1;" class="flex items-center">
							<Badge class={getMethodBadgeClass(item.method)}>
								{item.method}
							</Badge>
						</div>

						<!-- Path -->
						<div style="grid-column: span 3;" class="flex items-center">
							<span class="text-sm text-muted-foreground truncate font-mono">{item.path}</span>
						</div>

						<!-- Status -->
						<div style="grid-column: span 1;" class="flex items-center">
							<Badge class={getStatusBadgeClass(item.statusCode)}>
								{item.statusCode}
							</Badge>
						</div>

						<!-- IP Address -->
						<div style="grid-column: span 2;" class="flex items-center">
							<span class="text-sm font-mono text-muted-foreground">{item.ipAddress}</span>
						</div>

						<!-- Timestamp -->
						<div style="grid-column: span 2;" class="flex items-center">
							<span class="text-sm text-muted-foreground">{formatDateTime(item.createdAt)}</span>
						</div>
					</div>

					<!-- Mobile layout -->
					<div
						role="button"
						tabindex="0"
						class="flex md:hidden flex-col gap-1 p-4 hover:bg-muted/30 transition-all cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
						onclick={() => toggleExpand(item.id)}
						onkeydown={(e: KeyboardEvent) => { if (e.key === 'Enter' || e.key === ' ') toggleExpand(item.id); }}
					>
						<div class="flex items-center gap-2">
							<Badge class={getMethodBadgeClass(item.method)}>{item.method}</Badge>
							<Badge class={getStatusBadgeClass(item.statusCode)}>{item.statusCode}</Badge>
							<span class="text-xs text-muted-foreground ml-auto">{formatDateTime(item.createdAt)}</span>
						</div>
						<span class="text-sm truncate">{item.actorEmail}</span>
						<span class="text-xs text-muted-foreground font-mono truncate">{item.path}</span>
					</div>
					{#if expandedId === item.id}
						<div class="px-4 pb-4 border-t border-border/50">
							<p class="text-xs text-muted-foreground mb-2 mt-3 font-medium uppercase tracking-wide">Request Body</p>
							<pre class="text-xs bg-muted/50 rounded-md p-3 overflow-x-auto font-mono text-muted-foreground whitespace-pre-wrap break-all">{formatRequestBody(item.requestBody)}</pre>
						</div>
					{/if}
				</div>
			{/snippet}
		</DataTable>
	</div>
</main>
