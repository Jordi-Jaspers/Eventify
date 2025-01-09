<script lang="ts">
	import type { SubmitFunction } from '@sveltejs/kit';
	import { toast } from 'svelte-sonner';
	import { enhance } from '$app/forms';
	import { activeTeam, dashboards, user } from '$lib/store/global.js';
	import { DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, Root } from '$lib/components/ui/dialog';
	import { Label } from '$lib/components/ui/label';
	import { Input } from '$lib/components/ui/input';
	import { Textarea } from '$lib/components/ui/textarea';
	import { Submit } from '$lib/components/button';
	import { Button } from '$lib/components/ui/form';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { Separator } from '$lib/components/ui/separator';

	let {
		id = 0,
		name = '',
		description = '',
		global = false,
		team = {} as TeamResponse,
		isDropdownOpen = $bindable(false),
		isUpdateDialogOpen = $bindable(false)
	} = $props<{
		id: number;
		name: string;
		description: string;
		global: boolean;
		team: TeamResponse;
		isDropdownOpen: boolean;
		isUpdateDialogOpen: boolean;
	}>();

	let formData: DashboardUpdateRequest = $state({
		teamId: 0,
		name: name,
		description: description,
		global: global
	});

	let isAllowed: boolean = $derived(user.permissions.includes('WRITE_DASHBOARDS'));
	let isLoading = $state(false);
	const updateDashboard: SubmitFunction = () => {
		isLoading = true;
		return async ({ result }) => {
			isLoading = false;
			if (result.type === 'success' && result.data) {
				const apiResponse: ApiResponse = result.data.response;
				const dashboard: DashboardResponse = apiResponse.data;

				toast.success('Dashboard updated successfully');
				dashboards.updateDashboard(id, dashboard);
				isUpdateDialogOpen = false;
			}

			if (result.type === 'failure' && result.data) {
				const apiResponse: ApiResponse = result.data.response;
				toast.error(apiResponse.message);
			}
		};
	};
</script>

<Root bind:open={isUpdateDialogOpen} onOpenChange={() => (isDropdownOpen = false)}>
	<DialogContent>
		<DialogHeader>
			<DialogTitle>Update Dashboard</DialogTitle>
			<DialogDescription>Update the name, description, and global status for the dashboard.</DialogDescription>
		</DialogHeader>
		<form id="update-dashboard" action="?/updateDashboard" method="POST" use:enhance={updateDashboard}>
			<input type="hidden" name="id" value={id} />
			<div class="grid w-full items-center gap-4">
				<Input type="hidden" name="teamId" value={activeTeam.value.id} />
				<div class="grid gap-2">
					<div class="flex flex-row items-center justify-between">
						<Label>Name</Label>
					</div>
					<Input name="name" bind:value={formData.name} required />
				</div>

				<div class="grid gap-2">
					<div class="flex flex-row items-center justify-between">
						<Label>Description</Label>
					</div>
					<Textarea name="description" placeholder="Type your description here." bind:value={formData.description} required />
				</div>
				<Separator />
				<div class="grid gap-2">
					<div class="flex flex-row items-center space-x-2">
						<Label>Visibility</Label>
						<Checkbox name="global" bind:checked={formData.global} />
					</div>
					<p class="text-sm text-gray-500">
						Make this dashboard visible to everyone, including users outside your team. Otherwise, it will only be visible to members of the
						originating team '{team.name}'.
					</p>
				</div>
			</div>
			{#if !isAllowed}
				<Button variant="ghost" size="sm" class="mt-4" on:click={() => (isUpdateDialogOpen = false)}>Close</Button>
			{/if}
			<DialogFooter class="mt-4">
				<Submit {isLoading} isDisabled={isLoading} title="Update" form="update-dashboard" />
			</DialogFooter>
		</form>
	</DialogContent>
</Root>
