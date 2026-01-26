<script lang="ts">
	import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetDescription } from '$lib/components/ui/sheet';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import { FolderTree } from '@lucide/svelte';

	interface Props {
		open: boolean;
		onOpenChange: (open: boolean) => void;
		onSubmit: (name: string) => void;
	}

	let { open = $bindable(), onOpenChange, onSubmit }: Props = $props();

	let groupName: string = $state('');

	function handleSubmit(e: SubmitEvent): void {
		e.preventDefault();
		if (groupName.trim()) {
			onSubmit(groupName.trim());
			groupName = '';
			onOpenChange(false);
		}
	}

	function handleCancel(): void {
		groupName = '';
		onOpenChange(false);
	}
</script>

<Sheet {open} {onOpenChange}>
	<SheetContent side="right" class="sm:max-w-md bg-card/95 backdrop-blur-xl border-border/50">
		<SheetHeader>
			<SheetTitle class="flex items-center gap-2">
				<FolderTree class="h-5 w-5 text-accent" />
				Name Your Group
			</SheetTitle>
			<SheetDescription>
				Give this group a descriptive name
			</SheetDescription>
		</SheetHeader>

		<form onsubmit={handleSubmit} class="mt-6 space-y-6">
			<div class="space-y-2">
				<Label for="group-name">Group Name</Label>
				<Input
					id="group-name"
					bind:value={groupName}
					placeholder="e.g., Production Systems"
					class="bg-background/50 border-border"
					required
					autofocus
				/>
			</div>

			<div class="flex gap-3">
				<Button
					type="button"
					variant="outline"
					class="flex-1"
					onclick={handleCancel}
				>
					Cancel
				</Button>
				<Button
					type="submit"
					class="flex-1 bg-gradient-to-r from-accent to-accent/80 hover:opacity-90 text-accent-foreground"
					disabled={!groupName.trim()}
				>
					Create Group
				</Button>
			</div>
		</form>
	</SheetContent>
</Sheet>
