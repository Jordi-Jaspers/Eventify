<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import moment from 'moment/moment';

	let { name, global, lastUpdated, updatedBy } = $props<{
		name: string;
		global: boolean;
		lastUpdated: Date;
		updatedBy: string;
	}>();
</script>

<div class="mb-4">
	<h2 class="flex items-center space-x-2 border-none">
		<span>{name}</span>
		{#if global}
			<Badge variant="secondary">Public</Badge>
		{/if}
	</h2>
	<p class="text-sm italic text-muted-foreground">
		Updated: {moment(lastUpdated).fromNow()} by {updatedBy}
	</p>
</div>
