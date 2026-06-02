<script lang="ts">
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Badge } from '$lib/components/ui/badge';
	import { Building2, Check, RefreshCw } from '@lucide/svelte';
	import type { UserOrganizationResponse } from '$lib/api/models';
	import { getOrganizationalRoleBadgeClass } from '$lib/utils/role';

	interface Props {
		organizations: UserOrganizationResponse[];
		currentOrganization: UserOrganizationResponse | null;
		loading: boolean;
		error: string | null;
		onOrgSwitch: (orgId: number) => void;
		onRetry: () => void;
	}

	let {
		organizations,
		currentOrganization,
		loading,
		error,
		onOrgSwitch,
		onRetry
	}: Props = $props();

	function getOrgInitial(name: string | undefined): string {
		return name?.charAt(0)?.toUpperCase() || '?';
	}
</script>

{#if error}
	<div class="p-1">
		<DropdownMenu.Item class="cursor-pointer hover:bg-primary/10" onclick={onRetry}>
			<RefreshCw class="mr-2 h-4 w-4" />
			<span>Retry loading organizations</span>
		</DropdownMenu.Item>
	</div>
{:else if organizations.length > 0}
	<div class="p-1">
		<DropdownMenu.Label class="text-xs text-muted-foreground px-2 py-1.5">
			Switch Organization
		</DropdownMenu.Label>
		{#each organizations as org (org.organizationId)}
			<DropdownMenu.Item
				class="cursor-pointer hover:bg-primary/10 flex items-center gap-3 px-2 py-2"
				onclick={() => onOrgSwitch(org.organizationId)}
			>
				<div class="flex aspect-square size-8 items-center justify-center rounded-lg bg-gradient-to-br from-primary/80 to-accent/80 text-primary-foreground font-semibold text-xs">
					{getOrgInitial(org.organizationName)}
				</div>
				<div class="flex-1 min-w-0">
					<div class="font-medium truncate text-sm">{org.organizationName}</div>
					<Badge class="{getOrganizationalRoleBadgeClass(org.role)} w-fit text-[10px] px-1.5 py-0 leading-tight mt-0.5">
						{org.role}
					</Badge>
				</div>
				{#if org.organizationId === currentOrganization?.organizationId}
					<Check class="size-4 text-primary flex-shrink-0" />
				{/if}
			</DropdownMenu.Item>
		{/each}
	</div>
{:else if !loading}
	<div class="p-1">
		<div class="px-2 py-3 text-center">
			<Building2 class="size-5 mx-auto mb-1 text-muted-foreground/50" />
			<p class="text-xs text-muted-foreground">No organizations</p>
		</div>
	</div>
{/if}
