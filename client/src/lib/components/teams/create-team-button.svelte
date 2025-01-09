<script lang="ts">
	import { Plus } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import {
		Dialog,
		DialogContent,
		DialogDescription,
		DialogFooter,
		DialogHeader,
		DialogTitle,
		DialogTrigger
	} from '$lib/components/ui/dialog';
	import { Label } from '$lib/components/ui/label';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { applyAction, enhance } from '$app/forms';
	import { toast } from 'svelte-sonner';
	import { Submit } from '$lib/components/button';
	import { Textarea } from '$lib/components/ui/textarea';
	import { teams } from '$lib/store/global.js';
	import type { ServerResponse } from '$lib/models/server-response.svelte.js';

	let formData = $state<TeamRequest>({
		name: '',
		description: ''
	});
	let isOpen = $state(false);
	let isLoading = $state(false);

	const createTeam: SubmitFunction = () => {
		isLoading = true;
		return async ({ result }) => {
			isLoading = false;
			console.log(result);
			if (result.type === 'failure' && result.data) {
				const apiResponse: ServerResponse = result.data.response;
				toast.error(apiResponse.message);
			}

			if (result.type === 'success' && result.data) {
				const apiResponse: ServerResponse = result.data.response;
				toast.success('Team created successfully');

				const team: TeamResponse = apiResponse.data;
				teams.addTeam(team);
			}

			isOpen = false;
			formData = { name: '', description: '' };
			await applyAction(result);
		};
	};
</script>

<Dialog bind:open={isOpen}>
	<DialogTrigger>
		<Button>
			<Plus class="h-4 w-4" />
		</Button>
	</DialogTrigger>
	<DialogContent>
		<DialogHeader>
			<DialogTitle>Create New Team</DialogTitle>
			<DialogDescription>Enter the name and description for the new team.</DialogDescription>
		</DialogHeader>
		<form id="create-team" action="?/createTeam" method="POST" use:enhance={createTeam}>
			<div class="grid w-full items-center gap-4">
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
			</div>
			<DialogFooter class="mt-4">
				<Submit {isLoading} isDisabled={isLoading} title="Create" form="create-team" />
			</DialogFooter>
		</form>
	</DialogContent>
</Dialog>
