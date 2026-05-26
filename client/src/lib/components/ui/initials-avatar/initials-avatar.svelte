<script lang="ts">
	interface Props {
		initials: string;
		size?: 'sm' | 'md' | 'lg';
	}
	let { initials, size = 'md' }: Props = $props();

	const sizeClasses: Record<'sm' | 'md' | 'lg', string> = {
		sm: 'h-8 w-8 text-xs',
		md: 'h-10 w-10 text-sm',
		lg: 'h-11 w-11 text-base'
	};
</script>

<div
	class="{sizeClasses[size]} rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center flex-shrink-0"
>
	<span class="font-semibold text-primary">{initials}</span>
</div>
