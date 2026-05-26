<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import { Activity, Shield, ChevronDown } from '@lucide/svelte';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';

	interface Props {
		onScrollToFeatures: () => void;
	}

	let { onScrollToFeatures }: Props = $props();
</script>

<section id="hero" class="pt-32 pb-20 px-4 sm:px-6 lg:px-8">
	<div class="container mx-auto max-w-7xl">
		<div class="grid lg:grid-cols-2 gap-12 items-center">
			<!-- Hero Text -->
			<div class="animate-fade-in-up space-y-6">
				<h1 class="text-4xl sm:text-5xl lg:text-6xl font-bold leading-tight">
					<span class="text-primary">Event Monitoring</span>
					<br class="block" />
					<span class="text-foreground relative z-10">for Developers</span>
				</h1>
				<p class="text-lg sm:text-xl text-muted-foreground">
					Simple API. Real-time insights. Track events across your services with one API call.
				</p>
				<div class="flex flex-col sm:flex-row gap-4 pt-4">
					<Button
						href={CLIENT_ROUTES.REGISTER_PAGE.path}
						size="lg"
						class="text-base w-full sm:w-auto"
					>
						Get Started Free
					</Button>
					<Button
						onclick={onScrollToFeatures}
						size="lg"
						variant="outline"
						class="text-base w-full sm:w-auto"
					>
						Learn More
						<ChevronDown class="ml-2 h-4 w-4" />
					</Button>
				</div>
			</div>

			<!-- Hero Visual -->
			<div class="relative animate-fade-in mt-8 lg:mt-0">
				<div class="rounded-lg border border-border/50 bg-card/60 backdrop-blur-xl p-6 shadow-xl">
					<div class="space-y-4">
						<div class="flex items-center justify-between pb-4 border-b border-border/50">
							<div class="flex items-center gap-2">
								<Activity class="h-5 w-5 text-primary" />
								<span class="font-semibold">Live Events</span>
							</div>
							<div class="flex items-center gap-2">
								<PulseIndicator variant="green" size="md" />
								<span class="text-xs text-muted-foreground">Connected</span>
							</div>
						</div>

						<div class="space-y-3">
							<div class="animate-fade-in-up rounded-md border border-green-500/20 bg-green-500/10 p-3">
								<div class="flex items-center gap-2">
									<span class="text-green-500">●</span>
									<span class="text-sm font-mono">user.login</span>
								</div>
								<p class="text-xs text-muted-foreground mt-1">User authenticated successfully</p>
							</div>

							<div class="animate-fade-in-up rounded-md border border-blue-500/20 bg-blue-500/10 p-3" style="animation-delay: 0.2s;">
								<div class="flex items-center gap-2">
									<span class="text-blue-500">●</span>
									<span class="text-sm font-mono">payment.success</span>
								</div>
								<p class="text-xs text-muted-foreground mt-1">Payment processed - $99.99 USD</p>
							</div>

							<div class="animate-fade-in-up rounded-md border border-amber-500/20 bg-amber-500/10 p-3" style="animation-delay: 0.4s;">
								<div class="flex items-center gap-2">
									<span class="text-amber-500">●</span>
									<span class="text-sm font-mono">api.rate_limit</span>
								</div>
								<p class="text-xs text-muted-foreground mt-1">Rate limit warning - 80% capacity</p>
							</div>
						</div>
					</div>
				</div>

				<!-- Floating cards -->
				<div
					class="absolute -top-4 right-0 sm:-right-4 max-w-[200px] animate-fade-in-up rounded-md border border-border/50 bg-card/80 backdrop-blur-xl p-3 shadow-lg"
					style="animation-delay: 0.6s;"
				>
					<div class="flex items-center gap-2">
						<Shield class="h-4 w-4 text-primary" />
						<span class="text-xs font-medium">Secure & Encrypted</span>
					</div>
				</div>

				<div
					class="absolute -bottom-4 left-0 sm:-left-4 max-w-[180px] animate-fade-in-up rounded-md border border-border/50 bg-card/80 backdrop-blur-xl p-3 shadow-lg"
					style="animation-delay: 0.8s;"
				>
					<div class="flex items-center gap-2">
						<Activity class="h-4 w-4 text-primary" />
						<span class="text-xs font-medium">Real-Time Updates</span>
					</div>
				</div>
			</div>
		</div>
	</div>
</section>
