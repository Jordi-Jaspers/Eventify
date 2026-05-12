<script lang="ts">
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

<div class="flex items-center gap-1.5">
	<input
		type="date"
		value={fromDate}
		oninput={handleFromChange}
		class="h-8 w-[130px] rounded-md border border-border/50 bg-background/50 px-2 text-xs transition-colors focus:border-primary focus:outline-none"
	/>
	<span class="text-xs text-muted-foreground">–</span>
	<input
		type="date"
		value={toDate}
		oninput={handleToChange}
		class="h-8 w-[130px] rounded-md border border-border/50 bg-background/50 px-2 text-xs transition-colors focus:border-primary focus:outline-none"
	/>
</div>
