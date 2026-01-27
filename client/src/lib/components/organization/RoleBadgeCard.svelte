<script lang="ts">
	import { Card, CardHeader, CardDescription } from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Crown, Shield, User } from '@lucide/svelte';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import type { OrganizationalRole } from '$lib/api/models';

	interface Props {
		role: OrganizationalRole;
	}

	let { role }: Props = $props();

	const icon = $derived(role === 'OWNER' ? Crown : role === 'ADMIN' ? Shield : User);
	const Icon = $derived(icon);
</script>

<Card class="border-border/50 bg-card/50 backdrop-blur-xl shadow-2xl hover:shadow-primary/5 transition-all">
	<CardHeader>
		<div class="flex items-center gap-2 text-muted-foreground mb-2">
			<Icon class="w-4 h-4" />
			<CardDescription class="text-xs uppercase tracking-wide">Your Role</CardDescription>
		</div>
		<Badge class={`${getOrganizationalRoleBadgeClass(role)} text-sm py-1.5 px-3 w-fit`}>
			<Icon class="mr-1.5 h-3.5 w-3.5" />
			{role}
		</Badge>
	</CardHeader>
</Card>
