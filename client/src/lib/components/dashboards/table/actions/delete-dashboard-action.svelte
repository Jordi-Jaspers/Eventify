<script lang="ts">
	import { Action, Cancel, Content, Description, Footer, Header, Root, Title } from '$lib/components/ui/alert-dialog';
	import type { SubmitFunction } from '@sveltejs/kit';
	import { toast } from 'svelte-sonner';
	import { enhance } from '$app/forms';
	import { dashboards } from '$lib/store/global.js';

	let {
		id = 0,
		name = '',
		isDropdownOpen = $bindable(false),
		isDeleteDialogOpen = $bindable(false)
	} = $props<{
		id: number;
		name: string;
		isDropdownOpen: boolean;
		isDeleteDialogOpen: boolean;
	}>();

	const deleteDashboard: SubmitFunction = () => {
		return async ({ result }) => {
			if (result.type === 'success' && result.data) {
				toast.success('Dashboard deleted successfully');
				dashboards.removeDashboard(id);
				isDeleteDialogOpen = false;
			}

			if (result.type === 'failure' && result.data) {
				const apiResponse: ApiResponse = result.data.response;
				toast.error(apiResponse.message);
			}
		};
	};
</script>

<Root bind:open={isDeleteDialogOpen} onOpenChange={() => (isDropdownOpen = false)}>
	<Content>
		<Header>
			<Title>Are you absolutely sure?</Title>
			<Description>
				This action <b>cannot</b> be undone. This will permanently delete the dashboard "{name}" and its configuration for everyone.
			</Description>
		</Header>
		<Footer>
			<Cancel>Cancel</Cancel>
			<form method="POST" action="?/deleteDashboard" use:enhance={deleteDashboard}>
				<Action type="submit" class="bg-destructive text-destructive-foreground hover:bg-destructive/90">
					<input type="hidden" name="id" value={id} />
					Delete
				</Action>
			</form>
		</Footer>
	</Content>
</Root>
