<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Crown, Shield } from '@lucide/svelte';
	import type { OrganizationalRole } from '$lib/api/models';

	interface Props {
		role: OrganizationalRole;
	}

	let { role }: Props = $props();

	function getRoleBadgeClass(role: OrganizationalRole): string {
		switch (role) {
			case 'OWNER':
				return 'bg-gradient-to-r from-purple-500 to-purple-600 border-0 text-white';
			case 'ADMIN':
				return 'bg-blue-500/10 border-blue-500/50 text-blue-500';
			case 'MEMBER':
			default:
				return 'border-border/50 bg-background/50 text-muted-foreground';
		}
	}
</script>

<Badge class={getRoleBadgeClass(role)}>
	{#if role === 'OWNER'}
		<Crown class="mr-1 h-3 w-3" />
	{:else if role === 'ADMIN'}
		<Shield class="mr-1 h-3 w-3" />
	{/if}
	{role}
</Badge>
