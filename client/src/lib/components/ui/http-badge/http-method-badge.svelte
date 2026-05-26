<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';

	interface Props {
		method: string;
	}
	let { method }: Props = $props();

	function getMethodBadgeClass(m: string): string {
		switch (m) {
			case 'GET': return 'bg-blue-500/20 text-blue-400 border-blue-500/30';
			case 'POST': return 'bg-green-500/20 text-green-400 border-green-500/30';
			case 'PUT': return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
			case 'PATCH': return 'bg-orange-500/20 text-orange-400 border-orange-500/30';
			case 'DELETE': return 'bg-red-500/20 text-red-400 border-red-500/30';
			default: return 'bg-muted text-muted-foreground';
		}
	}
</script>

<Badge class={getMethodBadgeClass(method)}>{method}</Badge>
