<script lang="ts">
	import type { UserOrganizationResponse } from '$lib/api/models';
	import Badge from '$lib/components/ui/badge/badge.svelte';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';
	import { formatDateTime } from '$lib/utils/date';

	interface Props {
		membership: UserOrganizationResponse;
	}

	let { membership }: Props = $props();
</script>

<div class="p-4 rounded-lg bg-background/50 border border-border/50 hover:bg-background/70 hover:border-border transition-all duration-200 group">
	<div class="flex items-center justify-between gap-4">
		<div class="flex-1 min-w-0">
			<a 
				href="/organizations/{membership.organizationSlug}"
				class="text-sm font-medium text-foreground hover:text-primary transition-colors group-hover:underline decoration-primary/30"
			>
				{membership.organizationName}
			</a>
			<p class="text-xs text-muted-foreground mt-1">
				Joined {formatDateTime(membership.joinedAt)}
			</p>
		</div>
		<Badge class={getOrganizationalRoleBadgeClass(membership.role)}>
			{membership.role}
		</Badge>
	</div>
</div>
