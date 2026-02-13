<script lang="ts">
	import { cn } from '$lib/utils';

	type Variant = 'green' | 'primary' | 'red' | 'yellow' | 'blue';
	type Size = 'xs' | 'sm' | 'md' | 'lg';

	interface Props {
		variant?: Variant;
		size?: Size;
		label?: string;
		class?: string;
	}

	const { variant = 'green', size = 'sm', label, class: className }: Props = $props();

	const sizeClasses: Record<Size, string> = {
		xs: 'h-1.5 w-1.5',
		sm: 'h-2 w-2',
		md: 'h-2.5 w-2.5',
		lg: 'h-3 w-3'
	};

	const variantClasses: Record<Variant, string> = {
		green: 'bg-green-500',
		primary: 'bg-primary',
		red: 'bg-red-500',
		yellow: 'bg-yellow-500',
		blue: 'bg-blue-500'
	};

	const labelColorClasses: Record<Variant, string> = {
		green: 'text-green-500',
		primary: 'text-primary',
		red: 'text-red-500',
		yellow: 'text-yellow-500',
		blue: 'text-blue-500'
	};
</script>

{#if label}
	<span class={cn('flex items-center gap-1.5', labelColorClasses[variant], className)}>
		<span class={cn('rounded-full animate-pulse', sizeClasses[size], variantClasses[variant])}></span>
		<span class="text-xs font-medium">{label}</span>
	</span>
{:else}
	<span class={cn('rounded-full animate-pulse', sizeClasses[size], variantClasses[variant], className)}></span>
{/if}
