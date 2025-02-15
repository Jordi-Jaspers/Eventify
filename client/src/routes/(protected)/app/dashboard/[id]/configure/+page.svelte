<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { CLIENT_ROUTES } from '$lib/config/paths';
	import { goto } from '$app/navigation';
	import { toast } from 'svelte-sonner';
	import { FolderPlus, Plus, Trash2 } from 'lucide-svelte';
	import { Switch } from '$lib/components/ui/switch';
	import { Label } from '$lib/components/ui/label';
	import { Separator } from '$lib/components/ui/separator';
	import ChevronRight from 'lucide-svelte/icons/chevron-right';
	import Check from 'lucide-svelte/icons/check';
	import X from 'lucide-svelte/icons/x';
	import { DashboardConfigurationHeader } from '$lib/configuration';
	import { Submit } from '$lib/components/button';
	import type { SubmitFunction } from '@sveltejs/kit';
	import type { ServerResponse } from '$lib/models/server-response.svelte';
	import { applyAction, enhance } from '$app/forms';
	import { CheckSearchDialog } from '$lib/components/dashboards/index.js';

	let { data } = $props();
	let dashboard: DashboardResponse = $state(data.dashboard);
	let allConfiguredChecks: CheckResponse[] = $derived.by(() => {
		let allChecks: CheckResponse[] = dashboard.configuration.ungroupedChecks;
		dashboard.configuration.groups.forEach((group) => {
			allChecks = allChecks.concat(group.checks);
		});
		return allChecks;
	});

	let isLoading: boolean = $state(false);
	let expandedGroups: string[] = $state([]);

	function toggleGroup(groupName: string) {
		if (expandedGroups.includes(groupName)) {
			expandedGroups = expandedGroups.filter((group) => group !== groupName);
		} else {
			expandedGroups = [...expandedGroups, groupName];
		}
	}

	let isAddingGroup: boolean = $state(false);
	let newGroupName: string = $state('');
	let newGroupInputRef: HTMLInputElement | undefined = $state();

	function isGroupNameUnique(name: string): boolean {
		return !dashboard.configuration.groups.some((group) => group.name.toLowerCase() === name.trim().toLowerCase());
	}

	function handleAddCheck(check: CheckResponse, group: DashboardGroupResponse | null) {
		if (group) {
			group.checks = [...group.checks, check];
		} else {
			dashboard.configuration.ungroupedChecks = [...dashboard.configuration.ungroupedChecks, check];
		}
	}

	function handleDeleteCheck(check: CheckResponse, group: DashboardGroupResponse | null) {
		if (group) {
			group.checks = group.checks.filter((c) => c.id !== check.id);
		} else {
			dashboard.configuration.ungroupedChecks = dashboard.configuration.ungroupedChecks.filter((c) => c.id !== check.id);
		}
	}

	function handleAddGroup() {
		isAddingGroup = true;
		newGroupName = '';
		setTimeout(() => {
			newGroupInputRef?.focus();
		}, 0);
	}

	function handleDeleteGroup(toDelete: DashboardGroupResponse) {
		dashboard.configuration.groups = dashboard.configuration.groups.filter(
			(group) => group.name.toLowerCase() !== toDelete.name.toLowerCase()
		);
	}

	function handleSaveNewGroup() {
		const trimmedName = newGroupName.trim();
		if (trimmedName) {
			if (isGroupNameUnique(trimmedName)) {
				dashboard.configuration.groups = [
					...dashboard.configuration.groups,
					{
						name: trimmedName,
						checks: []
					}
				];
				isAddingGroup = false;
				newGroupName = '';
			} else {
				toast.error(`Group "${trimmedName}" already exists`);
				newGroupInputRef?.focus();
			}
		}
	}

	function handleCancelNewGroup() {
		isAddingGroup = false;
		newGroupName = '';
	}

	function handleNewGroupKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter') {
			handleSaveNewGroup();
		} else if (event.key === 'Escape') {
			handleCancelNewGroup();
		}
	}

	const configure: SubmitFunction = () => {
		isLoading = true;
		return async ({ result }) => {
			isLoading = false;
			if (result.type === 'failure' && result.data) {
				const apiResponse: ServerResponse = result.data.response;
				toast.error(apiResponse.message);
			}

			if (result.type === 'success' && result.data) {
				const apiResponse: ServerResponse = result.data.response;
				dashboard = apiResponse.data as DashboardResponse;
				toast.success('Dashboard configuration successfully updated.');
			}
			await applyAction(result);
		};
	};
</script>

<div class="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
	<DashboardConfigurationHeader
		name={dashboard.name}
		global={dashboard.global}
		lastUpdated={dashboard.lastUpdated}
		updatedBy={dashboard.updatedBy}
	/>

	<div class="grid h-full min-h-96 w-full grid-cols-8 gap-4">
		<div class="col-span-5 h-full w-full rounded-md border p-4 pt-2">
			<div class="mb-4 flex items-center justify-between">
				<h4 class="text-lg font-bold">Configuration</h4>
				<div class="space-x-2">
					<Button class="h-8" variant="outline" size="sm" onclick={() => handleAddGroup()}>
						<FolderPlus class="h-4 w-4" />
						<span>Group</span>
					</Button>
					<CheckSearchDialog variant="outline" {allConfiguredChecks} onCheckSelect={(check) => handleAddCheck(check, null)}>
						<Plus class="h-4 w-4" />
						<span>Check</span>
					</CheckSearchDialog>
				</div>
			</div>

			<div class="space-y-2">
				<div class="px-2 py-1 text-sm text-muted-foreground">Groups</div>
				<Separator />

				{#each dashboard.configuration.groups as group}
					<div class="overflow-hidden rounded-md border">
						<button
							class="flex w-full items-center justify-between px-2 py-1 transition-colors hover:bg-muted/50"
							onclick={() => toggleGroup(group.name)}
						>
							<span class="flex items-center space-x-2">
								<span class="transform transition-all duration-200 ease-in-out {expandedGroups.includes(group.name) ? 'rotate-90' : ''}">
									<ChevronRight class="h-4 w-4" />
								</span>
								<span class="text-sm">{group.name}</span>
							</span>
							<span>
								<CheckSearchDialog variant="ghost" {allConfiguredChecks} onCheckSelect={(check) => handleAddCheck(check, group)}>
									<Plus class="h-4 w-4" />
								</CheckSearchDialog>
								<Button
									variant="ghost"
									class="h-8 hover:bg-destructive/50 hover:text-destructive-foreground"
									size="sm"
									onclick={(e) => {
										e.stopPropagation();
										handleDeleteGroup(group);
									}}
								>
									<Trash2 class="h-4 w-4" />
								</Button>
							</span>
						</button>

						{#if expandedGroups.includes(group.name)}
							<div class="space-y-1 bg-muted/20 py-2 pl-6 pr-4">
								{#each group.checks as check}
									<div class="flex items-center justify-between rounded-sm px-2 py-1 hover:bg-muted/50">
										<span class="text-sm">{check.name}</span>
										<Button
											variant="ghost"
											class="h-8 hover:bg-destructive/50 hover:text-destructive-foreground"
											size="sm"
											onclick={() => {
												handleDeleteCheck(check, group);
											}}
										>
											<Trash2 class="h-4 w-4" />
										</Button>
									</div>
								{/each}
								{#if group.checks.length === 0}
									<div class="px-2 py-1 text-sm text-muted-foreground">No checks configured for this group</div>
								{/if}
							</div>
						{/if}
					</div>
				{/each}

				{#if isAddingGroup}
					<div class="overflow-hidden rounded-md border">
						<div class="flex items-center justify-between bg-muted/20 px-4 py-2">
							<div class="flex flex-1 items-center">
								<ChevronRight class="mr-2 h-4 w-4 text-muted-foreground" />
								<input
									bind:this={newGroupInputRef}
									bind:value={newGroupName}
									type="text"
									placeholder="Enter group name"
									onkeydown={handleNewGroupKeydown}
									class="flex-1 border-none bg-transparent text-sm focus:outline-none {newGroupName.trim() &&
									!isGroupNameUnique(newGroupName)
										? 'text-red-500'
										: ''}"
								/>
							</div>
							<div class="flex items-center space-x-1">
								<Button variant="ghost" size="sm" disabled={!newGroupName.trim()} onclick={handleSaveNewGroup}>
									<Check class="h-4 w-4" />
								</Button>
								<Button variant="ghost" size="sm" onclick={handleCancelNewGroup}>
									<X class="h-4 w-4" />
								</Button>
							</div>
						</div>
					</div>
				{/if}

				<div class="mt-4 space-y-1">
					<div class="px-2 py-1 text-sm text-muted-foreground">Ungrouped Checks</div>
					<Separator />

					{#each dashboard.configuration.ungroupedChecks as check}
						<div class="flex items-center justify-between rounded-sm px-2 py-1 hover:bg-muted/50">
							<span class="text-sm">{check.name}</span>
							<Button
								variant="ghost"
								class="h-8 hover:bg-destructive/50 hover:text-destructive-foreground"
								size="sm"
								onclick={() => {
									handleDeleteCheck(check, null);
								}}
							>
								<Trash2 class="h-4 w-4" />
							</Button>
						</div>
					{/each}

					{#if dashboard.configuration.ungroupedChecks.length === 0}
						<div class="px-2 py-1 text-sm text-muted-foreground">No ungrouped checks configured</div>
					{/if}
				</div>
			</div>
		</div>
		<div class="col-span-3 flex h-full w-full flex-col rounded-md border p-4 pt-2">
			<div class="pb-4">
				<h4 class="text-lg font-bold">Filters</h4>
				<p class="text-sm italic text-muted-foreground">This feature has not been implemented yet.</p>
			</div>

			<div class="flex h-full flex-col justify-between">
				<div class="flex flex-col space-y-2">
					<div class="flex items-center space-x-2">
						<Switch id="airplane-mode" disabled />
						<Label for="airplane-mode">Show Groups</Label>
					</div>
					<div class="flex items-center space-x-2">
						<Switch id="airplane-mode" disabled />
						<Label for="airplane-mode">Map Warnings</Label>
					</div>
					<div class="flex items-center space-x-2">
						<Switch id="airplane-mode" disabled />
						<Label for="airplane-mode">Critical Only</Label>
					</div>
				</div>
				<div class="flex flex-col space-y-2">
					<Separator class="my-4" />
					<div class="grid grid-cols-2 gap-2">
						<Button onclick={() => goto(CLIENT_ROUTES.DASHBOARDS_PAGE.path)}>Back</Button>
						<form id="configure-dashboard" action="?/configure" method="POST" use:enhance={configure}>
							<input type="hidden" name="configuration" value={JSON.stringify(dashboard.configuration)} />
							<Submit {isLoading} isDisabled={isLoading} title="Save" form="configure-dashboard" />
						</form>
					</div>
					<form id="configure-dashboard-and-monitor" action="?/configureAndMonitor" method="POST" use:enhance={configure}>
						<input type="hidden" name="configuration" value={JSON.stringify(dashboard.configuration)} />
						<Submit {isLoading} isDisabled={isLoading} title="Save & Monitor" form="configure-dashboard-and-monitor" />
					</form>
				</div>
			</div>
		</div>
	</div>
</div>
