<script lang="ts">
	import { AlertDialog as AlertDialogPrimitive } from 'bits-ui';
	import { buttonVariants } from '../button';
	import { cn } from '$lib/utils.js';

	let { ref = $bindable(null), class: className, ...restProps }: AlertDialogPrimitive.ActionProps = $props();
</script>

<AlertDialogPrimitive.Action bind:ref class={cn(buttonVariants(), className)} {...restProps} />
