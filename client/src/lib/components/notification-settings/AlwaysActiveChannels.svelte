<script lang="ts">
	import { Mail, Bell, Info } from '@lucide/svelte';
	import { Tooltip, TooltipContent, TooltipTrigger, TooltipProvider } from '$lib/components/ui/tooltip';

	interface Props {
		email?: string;
	}

	let { email = '' }: Props = $props();
</script>

<TooltipProvider>
	<div>
		<div class="flex items-center gap-2 mb-3">
			<p class="text-sm font-medium text-foreground">Always active</p>
			<Tooltip>
				<TooltipTrigger>
					{#snippet child({ props: triggerProps })}
						<button
							{...triggerProps}
							type="button"
							class="text-muted-foreground hover:text-foreground transition-colors"
							aria-label="Why are these always active?"
						>
							<Info class="w-3.5 h-3.5" />
						</button>
					{/snippet}
				</TooltipTrigger>
				<TooltipContent class="max-w-[260px] text-xs">
					Email and in-app notifications are always active to ensure you never miss critical alerts.
				</TooltipContent>
			</Tooltip>
		</div>

		<div class="space-y-1 pl-1">
			<!-- Email row -->
			<div class="flex items-center gap-3 py-2 px-3 rounded-md">
				<Mail class="w-4 h-4 text-muted-foreground shrink-0" />
				<span class="text-sm text-muted-foreground flex-1">
					Email{email ? ` (${email})` : ''}
				</span>
				<span class="text-xs text-muted-foreground/60 font-medium">Always on</span>
			</div>

			<!-- In-App row -->
			<div class="flex items-center gap-3 py-2 px-3 rounded-md">
				<Bell class="w-4 h-4 text-muted-foreground shrink-0" />
				<span class="text-sm text-muted-foreground flex-1">In-App</span>
				<span class="text-xs text-muted-foreground/60 font-medium">Always on</span>
			</div>
		</div>
	</div>
</TooltipProvider>
