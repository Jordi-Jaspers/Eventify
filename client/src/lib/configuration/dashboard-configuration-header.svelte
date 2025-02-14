<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import moment from 'moment/moment';

	let { name, global, lastUpdated, updatedBy, description } = $props<{
		name: string;
		global: boolean;
		lastUpdated: Date;
		updatedBy?: string;
		description?: string;
	}>();
</script>

<div class="mb-4">
	<h2 class="flex items-center space-x-2 border-none">
		<span>{name}</span>
		{#if global}
			<Badge variant="secondary">Public</Badge>
		{/if}
	</h2>
	{#if updatedBy}
		<p class="text-sm italic text-muted-foreground">
			Updated: {moment(lastUpdated).fromNow()} by {updatedBy}
		</p>
	{/if}

	{#if description}
		<p class="text-muted-foreground">{description}</p>
	{/if}
</div>
