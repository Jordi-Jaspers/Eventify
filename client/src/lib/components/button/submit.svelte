<script lang="ts">
import { Button } from '$lib/components/ui/button';

let { isLoading = false, isDisabled = false, title = "Submit", form = "submit" } = $props<{
	isLoading: boolean,
	isDisabled: boolean,
	title: string,
	form: string
}>();
</script>

<Button form={form} type="submit" class="w-full" disabled={isDisabled}>
	{#if isLoading}
		<div
			class="inline-block h-8 w-8 loading"
			role="status"
		>
		</div>
	{:else}
		<span>{title}</span>
	{/if}
</Button>
