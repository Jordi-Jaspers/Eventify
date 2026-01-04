<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import type { DateRange } from '../types';

	interface Props {
		value: DateRange | null;
		onChange: (value: DateRange | null) => void;
		placeholder?: string;
	}

	let { value = $bindable(null), onChange, placeholder = 'Select date range' }: Props = $props();

	let fromDate: string = $state(value?.from ?? '');
	let toDate: string = $state(value?.to ?? '');

	function handleFromChange(event: Event): void {
		const target = event.target as HTMLInputElement;
		fromDate = target.value;
		emitChange();
	}

	function handleToChange(event: Event): void {
		const target = event.target as HTMLInputElement;
		toDate = target.value;
		emitChange();
	}

	function emitChange(): void {
		if (!fromDate && !toDate) {
			onChange(null);
			return;
		}

		const dateRange: DateRange = {
			from: fromDate || undefined,
			to: toDate || undefined
		};
		onChange(dateRange);
	}
</script>

<div class="grid grid-cols-2 gap-2">
	<div class="space-y-1">
		<Label class="text-xs text-muted-foreground">From</Label>
		<Input
			type="date"
			value={fromDate}
			oninput={handleFromChange}
			class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
		/>
	</div>
	<div class="space-y-1">
		<Label class="text-xs text-muted-foreground">To</Label>
		<Input
			type="date"
			value={toDate}
			oninput={handleToChange}
			class="bg-background/50 border-border transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
		/>
	</div>
</div>
