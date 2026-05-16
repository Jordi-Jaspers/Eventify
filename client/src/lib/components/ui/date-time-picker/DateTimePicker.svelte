<script lang="ts">
	import { Calendar as CalendarIcon, Clock } from '@lucide/svelte';
	import { Calendar } from '$lib/components/ui/calendar';
	import * as Popover from '$lib/components/ui/popover';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import {
		CalendarDate,
		CalendarDateTime,
		getLocalTimeZone,
		today,
		type DateValue
	} from '@internationalized/date';

	interface Props {
		/** ISO string value */
		value: string;
		/** Called when value changes with new ISO string */
		onValueChange: (isoString: string) => void;
		/** Label text */
		label?: string;
		/** Placeholder text */
		placeholder?: string;
		/** ID for accessibility */
		id?: string;
		/** Disable the picker */
		disabled?: boolean;
		/** Minimum selectable date */
		minValue?: DateValue;
		/** Maximum selectable date */
		maxValue?: DateValue;
		/** Date-only mode: hides time input, outputs YYYY-MM-DD */
		dateOnly?: boolean;
	}

	let {
		value,
		onValueChange,
		label,
		placeholder,
		id = 'datetime-picker',
		disabled = false,
		minValue,
		maxValue,
		dateOnly = false
	}: Props = $props();

	const effectivePlaceholder: string = $derived(placeholder ?? (dateOnly ? 'Select date' : 'Select date and time'));

	let open: boolean = $state(false);

	// Parse ISO string to CalendarDate for the calendar
	const calendarValue: CalendarDate | undefined = $derived.by(() => {
		if (!value) return undefined;
		try {
			const date: Date = new Date(value);
			if (isNaN(date.getTime())) return undefined;
			return new CalendarDate(date.getFullYear(), date.getMonth() + 1, date.getDate());
		} catch {
			return undefined;
		}
	});

	// Extract time from ISO string
	const timeValue: string = $derived.by(() => {
		if (!value) return '';
		try {
			const date: Date = new Date(value);
			if (isNaN(date.getTime())) return '';
			const hours: string = String(date.getHours()).padStart(2, '0');
			const minutes: string = String(date.getMinutes()).padStart(2, '0');
			return `${hours}:${minutes}`;
		} catch {
			return '';
		}
	});

	// Format display value
	const displayValue: string = $derived.by(() => {
		if (!value) return '';
		try {
			const date: Date = new Date(value);
			if (isNaN(date.getTime())) return '';
			if (dateOnly) {
				return date.toLocaleDateString('en-GB', {
					day: '2-digit',
					month: 'short',
					year: 'numeric'
				});
			}
			return date.toLocaleString('en-GB', {
				day: '2-digit',
				month: 'short',
				year: 'numeric',
				hour: '2-digit',
				minute: '2-digit',
				hour12: false
			});
		} catch {
			return '';
		}
	});

	function handleDateSelect(newDate: DateValue | undefined): void {
		if (!newDate) return;

		if (dateOnly) {
			const iso: string = `${String(newDate.year)}-${String(newDate.month).padStart(2, '0')}-${String(newDate.day).padStart(2, '0')}`;
			onValueChange(iso);
			open = false;
			return;
		}

		// Preserve existing time or use current time
		let hours: number = 0;
		let minutes: number = 0;

		if (value) {
			const existingDate: Date = new Date(value);
			if (!isNaN(existingDate.getTime())) {
				hours = existingDate.getHours();
				minutes = existingDate.getMinutes();
			}
		} else {
			// Default to current time if no existing value
			const now: Date = new Date();
			hours = now.getHours();
			minutes = now.getMinutes();
		}

		const dateTime: CalendarDateTime = new CalendarDateTime(
			newDate.year,
			newDate.month,
			newDate.day,
			hours,
			minutes
		);

		onValueChange(dateTime.toDate(getLocalTimeZone()).toISOString());
	}

	function handleTimeChange(event: Event): void {
		const input: HTMLInputElement = event.target as HTMLInputElement;
		const [hours, minutes] = input.value.split(':').map(Number);

		if (isNaN(hours) || isNaN(minutes)) return;

		// Use existing date or today
		let year: number, month: number, day: number;

		if (calendarValue) {
			year = calendarValue.year;
			month = calendarValue.month;
			day = calendarValue.day;
		} else {
			const now: Date = new Date();
			year = now.getFullYear();
			month = now.getMonth() + 1;
			day = now.getDate();
		}

		const dateTime: CalendarDateTime = new CalendarDateTime(year, month, day, hours, minutes);
		onValueChange(dateTime.toDate(getLocalTimeZone()).toISOString());
	}

	function handleClear(): void {
		onValueChange('');
		open = false;
	}

	function handleNow(): void {
		if (dateOnly) {
			const now: Date = new Date();
			const iso: string = `${String(now.getFullYear())}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
			onValueChange(iso);
		} else {
			onValueChange(new Date().toISOString());
		}
	}
</script>

<div class="space-y-1.5">
	{#if label}
		<Label for={id} class="text-xs font-medium text-muted-foreground">{label}</Label>
	{/if}

	<Popover.Root bind:open>
		<Popover.Trigger {id} {disabled}>
			{#snippet child({ props }: { props: Record<string, unknown> })}
				<Button
					{...props}
					variant="outline"
					class="w-full justify-start text-left font-normal h-9 px-3 border-border/30 bg-background/50 hover:bg-background/80 hover:border-border/50 transition-all {!value
						? 'text-muted-foreground'
						: ''}"
					{disabled}
				>
					<CalendarIcon class="mr-2 h-3.5 w-3.5 shrink-0 text-muted-foreground" />
					<span class="truncate text-xs">
						{displayValue || effectivePlaceholder}
					</span>
				</Button>
			{/snippet}
		</Popover.Trigger>

		<Popover.Content
			class="w-auto p-0 border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl"
			align="start"
			sideOffset={4}
		>
			<!-- Calendar -->
			<div class="p-3 border-b border-border/30">
				<Calendar
					type="single"
					value={calendarValue}
					onValueChange={handleDateSelect}
					captionLayout="dropdown"
					{minValue}
					{maxValue}
				/>
			</div>

		<!-- Time Input -->
		{#if !dateOnly}
		<div class="p-3 border-b border-border/30">
			<div class="flex items-center gap-2">
				<Clock class="h-3.5 w-3.5 text-muted-foreground shrink-0" />
				<Label for="{id}-time" class="text-xs font-medium text-muted-foreground shrink-0"
					>Time</Label
				>
				<Input
					id="{id}-time"
					type="time"
					value={timeValue}
					onchange={handleTimeChange}
					lang="en-GB"
					class="flex-1 h-8 text-xs bg-background/50 border-border/30 focus:border-primary/50 focus:ring-2 focus:ring-primary/20 [&::-webkit-calendar-picker-indicator]:hidden"
				/>
			</div>
		</div>
		{/if}

			<!-- Quick Actions -->
			<div class="p-2 flex items-center justify-between gap-2 bg-muted/5">
				<Button
					variant="ghost"
					size="sm"
				onclick={handleNow}
				class="h-7 text-xs text-muted-foreground hover:text-foreground"
			>
				{dateOnly ? 'Today' : 'Now'}
				</Button>
				<div class="flex items-center gap-1">
					{#if value}
						<Button
							variant="ghost"
							size="sm"
							onclick={handleClear}
							class="h-7 text-xs text-muted-foreground hover:text-destructive"
						>
							Clear
						</Button>
					{/if}
					<Button
						variant="default"
						size="sm"
						onclick={() => (open = false)}
						class="h-7 text-xs"
					>
						Done
					</Button>
				</div>
			</div>
		</Popover.Content>
	</Popover.Root>
</div>
