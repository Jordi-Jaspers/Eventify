<script lang="ts">
	import { Clock } from '@lucide/svelte';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';

	interface Props {
		id: string;
		timeValue: string;
		onTimeChange: (event: Event) => void;
	}

	let { id, timeValue, onTimeChange }: Props = $props();
</script>

<div class="p-3 border-b border-border/30">
	<div class="flex items-center gap-2">
		<Clock class="h-3.5 w-3.5 text-muted-foreground shrink-0" />
		<Label for="{id}-time" class="text-xs font-medium text-muted-foreground shrink-0">Time</Label>
		<Input
			id="{id}-time"
			type="time"
			value={timeValue}
			onchange={onTimeChange}
			lang="en-GB"
			class="flex-1 h-8 text-xs bg-background/50 border-border/30 focus:border-primary/50 focus:ring-2 focus:ring-primary/20 [&::-webkit-calendar-picker-indicator]:hidden"
		/>
	</div>
</div>
