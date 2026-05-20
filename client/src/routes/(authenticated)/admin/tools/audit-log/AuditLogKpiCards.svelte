<script lang="ts">
	import { Activity, AlertTriangle, PenLine, Users } from '@lucide/svelte';
	import { StatCard } from '$lib/components/ui/stat-card';
	import type { AuditLogStatsResponse } from '$lib/api/models';

	interface Props {
		stats: AuditLogStatsResponse | null;
		statsLoading: boolean;
		errorRate: string;
		onErrorClick: () => void;
		onMutationClick: () => void;
	}

	let { stats, statsLoading, errorRate, onErrorClick, onMutationClick }: Props = $props();
</script>

<div class="grid grid-cols-2 md:grid-cols-4 gap-3">
	<StatCard
		title="Total Requests"
		value={stats?.totalRequests?.toLocaleString() ?? '0'}
		icon={Activity}
		variant="blue"
		loading={statsLoading}
	/>

	<button class="text-left" onclick={onErrorClick} title="Click to filter errors">
		<StatCard
			title="Error Rate"
			value="{errorRate}%"
			icon={AlertTriangle}
			variant="red"
			loading={statsLoading}
		/>
	</button>

	<button class="text-left" onclick={onMutationClick} title="Click to filter mutations">
		<StatCard
			title="Mutations"
			value={stats?.mutationCount?.toLocaleString() ?? '0'}
			icon={PenLine}
			variant="purple"
			loading={statsLoading}
		/>
	</button>

	<StatCard
		title="Unique Actors"
		value={stats?.uniqueActors?.toLocaleString() ?? '0'}
		icon={Users}
		variant="green"
		loading={statsLoading}
	/>
</div>
