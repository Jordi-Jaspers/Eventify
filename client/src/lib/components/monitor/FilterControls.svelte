<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { Label } from '$lib/components/ui/label';
	import { Checkbox } from '$lib/components/ui/checkbox';
	import { AlertCircle, ArrowDownUp, FolderTree, RotateCcw } from '@lucide/svelte';

	interface Props {
		onlyCritical: boolean;
		sortBySeverity: boolean;
		groupedView: boolean;
		onToggleOnlyCritical: () => void;
		onToggleSortBySeverity: () => void;
		onToggleGroupedView: () => void;
		onReset: () => void;
		disabled?: boolean;
	}

	let {
		onlyCritical,
		sortBySeverity,
		groupedView,
		onToggleOnlyCritical,
		onToggleSortBySeverity,
		onToggleGroupedView,
		onReset,
		disabled = false
	}: Props = $props();
</script>

<div class="flex flex-wrap items-center gap-6">
	<!-- Only Critical -->
	<div class="flex items-center gap-2">
		<Checkbox
			id="only-critical"
			checked={onlyCritical}
			onCheckedChange={onToggleOnlyCritical}
			disabled={disabled}
		/>
		<Label
			for="only-critical"
			class="flex items-center gap-2 text-sm font-normal cursor-pointer"
		>
			<AlertCircle class="h-4 w-4 text-red-500" />
			Only Critical
		</Label>
	</div>

	<!-- Sort by Severity -->
	<div class="flex items-center gap-2">
		<Checkbox
			id="sort-severity"
			checked={sortBySeverity}
			onCheckedChange={onToggleSortBySeverity}
			disabled={disabled}
		/>
		<Label
			for="sort-severity"
			class="flex items-center gap-2 text-sm font-normal cursor-pointer"
		>
			<ArrowDownUp class="h-4 w-4 text-muted-foreground" />
			Sort by Severity
		</Label>
	</div>

	<!-- Grouped View -->
	<div class="flex items-center gap-2">
		<Checkbox
			id="grouped-view"
			checked={groupedView}
			onCheckedChange={onToggleGroupedView}
			disabled={disabled}
		/>
		<Label
			for="grouped-view"
			class="flex items-center gap-2 text-sm font-normal cursor-pointer"
		>
			<FolderTree class="h-4 w-4 text-muted-foreground" />
			Grouped View
		</Label>
	</div>

	<!-- Reset button -->
	<Button
		variant="ghost"
		size="sm"
		onclick={onReset}
		disabled={disabled}
		class="gap-2 ml-auto"
	>
		<RotateCcw class="h-3 w-3" />
		Reset to Defaults
	</Button>
</div>
