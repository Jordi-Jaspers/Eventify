<script lang="ts">
	import type { DateRange } from '../types';
	import { DateTimePicker } from '$lib/components/ui/date-time-picker';

	interface Props {
		value: DateRange | null;
		onChange: (value: DateRange | null) => void;
		placeholder?: string;
	}

	let { value = $bindable(null), onChange, placeholder = 'Select date range' }: Props = $props();

	let fromDate: string = $state(value?.from ?? '');
	let toDate: string = $state(value?.to ?? '');

	function handleFromChange(newValue: string): void {
		fromDate = newValue;
		emitChange();
	}

	function handleToChange(newValue: string): void {
		toDate = newValue;
		emitChange();
	}

	function emitChange(): void {
		if (!fromDate && !toDate) {
			onChange(null);
			return;
		}

		const dateRange: DateRange = {
			from: fromDate ? (fromDate.includes('T') ? fromDate : `${fromDate}T00:00:00`) : undefined,
			to: toDate ? (toDate.includes('T') ? toDate : `${toDate}T23:59:59`) : undefined
		};
		onChange(dateRange);
	}
</script>

<div class="flex items-center gap-1.5">
	<DateTimePicker
		value={fromDate}
		onValueChange={handleFromChange}
		placeholder="From date"
		id="date-filter-from"
		dateOnly={false}
	/>
	<span class="text-xs text-muted-foreground shrink-0">–</span>
	<DateTimePicker
		value={toDate}
		onValueChange={handleToChange}
		placeholder="To date"
		id="date-filter-to"
		dateOnly={false}
	/>
</div>
