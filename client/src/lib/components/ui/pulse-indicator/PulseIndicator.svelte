<script lang="ts">
	import { cn } from '$lib/utils';
	import {
		SIZE_CLASSES,
		VARIANT_CLASSES,
		LABEL_COLOR_CLASSES,
		type PulseIndicatorSize,
		type PulseIndicatorVariant
	} from './constants';

	/**
	 * PulseIndicator component - Animated status indicator with optional label
	 *
	 * @example
	 * ```svelte
	 * <PulseIndicator variant="green" size="sm" label="Active" />
	 * <PulseIndicator variant="red" size="md" />
	 * ```
	 */
	interface Props {
		/** Color variant of the indicator */
		variant?: PulseIndicatorVariant;
		/** Size of the indicator dot */
		size?: PulseIndicatorSize;
		/** Optional text label to display next to the indicator */
		label?: string;
		/** Additional CSS classes to apply */
		class?: string;
	}

	const { variant = 'green', size = 'sm', label, class: className }: Props = $props();

	const indicatorClasses: string = $derived(
		cn('rounded-full animate-pulse', SIZE_CLASSES[size], VARIANT_CLASSES[variant])
	);

	const wrapperClasses: string = $derived(
		cn('flex items-center gap-1.5', label && LABEL_COLOR_CLASSES[variant], className)
	);
</script>

{#if label}
	<span class={wrapperClasses}>
		<span class={indicatorClasses}></span>
		<span class="text-xs font-medium">{label}</span>
	</span>
{:else}
	<span class={cn(indicatorClasses, className)}></span>
{/if}
