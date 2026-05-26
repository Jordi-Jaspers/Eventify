<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';

	interface Props {
		status: number;
	}
	let { status }: Props = $props();

	function getStatusBadgeClass(statusCode: number): string {
		if (statusCode >= 200 && statusCode < 300) return 'bg-green-500/20 text-green-400 border-green-500/30';
		if (statusCode >= 400 && statusCode < 500) return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
		if (statusCode >= 500) return 'bg-red-500/20 text-red-400 border-red-500/30';
		return 'bg-muted text-muted-foreground';
	}
</script>

<Badge class={getStatusBadgeClass(status)}>{status}</Badge>
