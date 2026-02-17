<script lang="ts">
	import { Input } from '$lib/components/ui/input';
	import { Textarea } from '$lib/components/ui/textarea';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Pencil, Check, X, LoaderCircle } from '@lucide/svelte';
	import type { Snippet } from 'svelte';

	interface Props {
		value: string;
		editing: boolean;
		saving: boolean;
		tempValue: string;
		canEdit?: boolean;
		multiline?: boolean;
		placeholder?: string;
		onStartEdit: () => void;
		onSave: () => void;
		onCancel: () => void;
		onTempValueChange: (value: string) => void;
		onKeydown?: (event: KeyboardEvent) => void;
		class?: string;
		inputClass?: string;
		children?: Snippet;
	}

	let {
		value,
		editing,
		saving,
		tempValue,
		canEdit = true,
		multiline = false,
		placeholder = '',
		onStartEdit,
		onSave,
		onCancel,
		onTempValueChange,
		onKeydown,
		class: className = '',
		inputClass = '',
		children
	}: Props = $props();

	function handleInput(event: Event): void {
		const target = event.target as HTMLInputElement | HTMLTextAreaElement;
		onTempValueChange(target.value);
	}

	function handleKeydownInternal(event: KeyboardEvent): void {
		if (onKeydown) {
			onKeydown(event);
		}
	}
</script>

{#if editing}
	<div class="flex items-start gap-2 {className}">
		{#if multiline}
			<Textarea
				value={tempValue}
				oninput={handleInput}
				onkeydown={handleKeydownInternal}
				disabled={saving}
				{placeholder}
				class="resize-none min-h-[80px] {inputClass}"
				autofocus
			/>
		{:else}
			<Input
				value={tempValue}
				oninput={handleInput}
				onkeydown={handleKeydownInternal}
				disabled={saving}
				{placeholder}
				class={inputClass}
				autofocus
			/>
		{/if}
		<div class="flex items-center gap-1 shrink-0">
			<Button
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-green-500 hover:text-green-600 hover:bg-green-500/10"
				onclick={onSave}
				disabled={saving || !tempValue.trim()}
			>
				{#if saving}
					<LoaderCircle class="h-4 w-4 animate-spin" />
				{:else}
					<Check class="h-4 w-4" />
				{/if}
			</Button>
			<Button
				variant="ghost"
				size="icon"
				class="h-8 w-8 text-muted-foreground hover:text-destructive hover:bg-destructive/10"
				onclick={onCancel}
				disabled={saving}
			>
				<X class="h-4 w-4" />
			</Button>
		</div>
	</div>
{:else}
	<button
		class="group flex items-center gap-2 hover:text-primary transition-colors {className}"
		onclick={onStartEdit}
		disabled={!canEdit}
	>
		{#if children}
			{@render children()}
		{:else}
			<span class="text-sm {!value ? 'text-muted-foreground italic' : ''}">{value || placeholder}</span>
		{/if}
		{#if canEdit}
			<Pencil class="h-3.5 w-3.5 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground" />
		{/if}
	</button>
{/if}
