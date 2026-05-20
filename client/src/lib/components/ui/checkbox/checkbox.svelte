<script lang="ts">
	import { Checkbox as CheckboxPrimitive } from 'bits-ui';
	import { Check, Minus } from '@lucide/svelte';
	import { cn } from '$lib/utils.js';

	interface Props {
		checked?: boolean;
		indeterminate?: boolean;
		disabled?: boolean;
		required?: boolean;
		name?: string;
		value?: string;
		id?: string;
		class?: string;
		'aria-label'?: string;
		onclick?: (event: MouseEvent) => void;
		onCheckedChange?: (checked: boolean) => void;
	}

	let {
		checked = $bindable(false),
		indeterminate = false,
		disabled = false,
		required = false,
		name,
		value,
		id,
		class: className,
		'aria-label': ariaLabel,
		onclick,
		onCheckedChange,
		...restProps
	}: Props = $props();
</script>

<CheckboxPrimitive.Root
	{checked}
	{indeterminate}
	{disabled}
	{required}
	{name}
	{value}
	{id}
	{onclick}
	aria-label={ariaLabel}
	onCheckedChange={onCheckedChange}
	class={cn(
		'peer h-4 w-4 shrink-0 rounded-sm border border-primary ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground data-[state=indeterminate]:bg-primary data-[state=indeterminate]:text-primary-foreground',
		className
	)}
	{...restProps}
>
	{#if indeterminate}
		<div class="flex h-full w-full items-center justify-center text-current">
			<Minus class="h-3.5 w-3.5" />
		</div>
	{:else if checked}
		<div class="flex h-full w-full items-center justify-center text-current">
			<Check class="h-3.5 w-3.5" />
		</div>
	{/if}
</CheckboxPrimitive.Root>
