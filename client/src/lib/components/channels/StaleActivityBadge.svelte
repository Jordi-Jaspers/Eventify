<script lang="ts">
	import { Badge } from '$lib/components/ui/badge';
	import * as Tooltip from '$lib/components/ui/tooltip';
	import { getStaleBadgeProps } from '$lib/utils/channel';

	interface Props {
		isStale: boolean;
	}

	let { isStale }: Props = $props();
	
	const badgeProps = $derived(getStaleBadgeProps(isStale));
</script>

{#if badgeProps.show}
	<Tooltip.Root>
		<Tooltip.Trigger>
			<Badge class={badgeProps.className}>
				{badgeProps.label}
			</Badge>
		</Tooltip.Trigger>
		<Tooltip.Content>
			<p>{badgeProps.tooltip}</p>
		</Tooltip.Content>
	</Tooltip.Root>
{/if}
