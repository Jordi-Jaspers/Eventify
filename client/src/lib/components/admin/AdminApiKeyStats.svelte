<script lang="ts">
	import { Key, Users, Building2, TrendingUp, Clock, ShieldAlert, BarChart3, AlertTriangle } from '@lucide/svelte';
	import { StatCard } from '$lib/components/ui/stat-card';
	import { ErrorAlert } from '$lib/components/ui/error-alert';
	import type { AdminApiKeyStatsResponse } from '$lib/api/models';
	import { formatNumber } from '$lib/components/admin';

	interface Props {
		stats: AdminApiKeyStatsResponse | null;
		loading: boolean;
		error: string | null;
		onRetry: () => void;
	}

	let { stats, loading, error, onRetry }: Props = $props();
</script>

{#if error && !loading}
	<ErrorAlert message={error} {onRetry} />
{/if}

<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
	<StatCard title="Total Keys" value={formatNumber(stats?.totalKeys)} icon={Key} {loading} variant="primary" />
	<StatCard title="User Keys" value={formatNumber(stats?.userKeys)} icon={Users} {loading} variant="blue" />
	<StatCard title="Org Keys" value={formatNumber(stats?.organizationKeys)} icon={Building2} {loading} variant="purple" />
	<StatCard title="Created This Month" value={formatNumber(stats?.createdThisMonth)} icon={TrendingUp} {loading} variant="green" />
	<StatCard title="Never Used" value={formatNumber(stats?.neverUsedKeys)} icon={Clock} {loading} variant="yellow" />
	<StatCard title="Expiring (30d)" value={formatNumber(stats?.expiringNext30Days)} icon={AlertTriangle} {loading} variant="orange" />
	<StatCard title="Revoked This Month" value={formatNumber(stats?.revokedThisMonth)} icon={ShieldAlert} {loading} variant="red" />
	<StatCard
		title="Top Key"
		value={stats?.topKeysByUsage && stats.topKeysByUsage.length > 0
			? formatNumber(stats.topKeysByUsage[0].totalRequests)
			: 'No usage data'}
		subtitle={stats?.topKeysByUsage?.[0]?.name}
		icon={BarChart3}
		{loading}
		variant="accent"
	/>
</div>
