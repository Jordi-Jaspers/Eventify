<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import { Crown, Shield } from '@lucide/svelte';
	import type { OrganizationalRole } from '$lib/api/models';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';

	interface Props {
		role: OrganizationalRole;
	}

	let { role }: Props = $props();
</script>

<Badge class={getOrganizationalRoleBadgeClass(role)}>
	{#if role === 'OWNER'}
		<Crown class="mr-1 h-3 w-3" />
	{:else if role === 'ADMIN'}
		<Shield class="mr-1 h-3 w-3" />
	{/if}
	{role}
</Badge>
