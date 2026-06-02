<script lang="ts">
	import {
		Activity,
		Building2,
		GitBranch,
		Code2,
		Users,
		Shield,
		Plug,
		Radio,
		BarChart3,
		ChevronDown,
		ChevronRight
	} from '@lucide/svelte';
	import { type Component } from 'svelte';

	interface FeatureCard {
		icon: Component;
		title: string;
		description: string;
	}

	interface HowItWorksStep {
		icon: Component;
		title: string;
		description: string;
	}

	const features: FeatureCard[] = [
		{
			icon: Activity,
			title: 'Real-Time Streaming',
			description: 'WebSocket-powered live updates deliver events the moment they happen. No polling, no delays.'
		},
		{
			icon: Building2,
			title: 'Multi-Organization Support',
			description: 'Manage multiple organizations with isolated data, separate API keys, and custom retention policies.'
		},
		{
			icon: GitBranch,
			title: 'Channel-Based Routing',
			description: 'Route events to specific channels for organized monitoring. Filter and search with precision.'
		},
		{
			icon: Code2,
			title: 'Developer-First API',
			description: 'RESTful API with comprehensive documentation. Integrate in minutes with any language or framework.'
		},
		{
			icon: Users,
			title: 'Team Collaboration',
			description: 'Role-based access control, team dashboards, and shared insights. Keep everyone in sync.'
		},
		{
			icon: Shield,
			title: 'Enterprise Security',
			description: 'API key rotation, audit logs, rate limiting, and encrypted storage. Built for compliance.'
		}
	];

	const steps: HowItWorksStep[] = [
		{
			icon: Plug,
			title: 'Create a Channel',
			description: 'Organize events by service, environment, or team. Channels keep your event streams structured.'
		},
		{
			icon: Radio,
			title: 'Send Events',
			description: 'One API call from any language. No SDK required — just a simple HTTP POST.'
		},
		{
			icon: BarChart3,
			title: 'Monitor & React',
			description: 'Live dashboards, severity tracking, and trend analysis. Know what\'s happening in real time.'
		}
	];
</script>

<!-- Features Section -->
<section id="features" class="py-20 px-4 sm:px-6 lg:px-8 bg-muted/30">
	<div class="container mx-auto max-w-7xl">
		<div class="text-center mb-16 animate-fade-in-up">
			<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold mb-4 text-foreground">
				Powerful Features
			</h2>
			<p class="text-lg text-muted-foreground max-w-2xl mx-auto">
				Everything you need to monitor, analyze, and respond to events in real-time
			</p>
		</div>

		<div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
			{#each features as { icon: FeatureIcon, title, description }, i}
				<div
					class="group rounded-lg border border-border/50 bg-card/50 backdrop-blur-xl shadow-lg p-6 transition-all hover:border-primary/50 hover:shadow-xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: {i * 0.1}s;"
				>
					<div class="mb-4 inline-flex rounded-md bg-primary/10 p-3">
						<FeatureIcon class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">{title}</h3>
					<p class="text-muted-foreground">{description}</p>
				</div>
			{/each}
		</div>
	</div>
</section>

<!-- How It Works Section -->
<section id="how-it-works" class="py-20 px-4 sm:px-6 lg:px-8">
	<div class="container mx-auto max-w-7xl">
		<div class="text-center mb-16 animate-fade-in-up">
			<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold mb-4 text-foreground">
				How It Works
			</h2>
			<p class="text-lg text-muted-foreground max-w-2xl mx-auto">
				Get started in three simple steps
			</p>
		</div>

		<div class="flex flex-col md:flex-row items-stretch gap-0">
			{#each steps as { icon: StepIcon, title, description }, i}
				<div class="flex-1 animate-fade-in-up" style="animation-delay: {i * 0.2}s;">
					<div class="relative flex flex-col h-full bg-card/50 backdrop-blur-xl border border-border/50 shadow-md rounded-lg p-8 overflow-hidden">
						<span class="absolute top-4 right-6 text-8xl font-black text-primary/8 select-none leading-none">{i + 1}</span>
						<div class="inline-flex items-center justify-center w-12 h-12 rounded-md bg-primary/10 border border-primary/20 mb-5">
							<StepIcon class="h-6 w-6 text-primary" />
						</div>
						<h3 class="text-xl font-semibold mb-3">{title}</h3>
						<p class="text-muted-foreground text-sm leading-relaxed">{description}</p>
					</div>
				</div>

				{#if i < steps.length - 1}
					<div class="hidden md:flex items-center justify-center px-2 shrink-0">
						<ChevronRight class="h-6 w-6 text-primary/40" />
					</div>
					<div class="flex md:hidden items-center justify-center py-2">
						<ChevronDown class="h-6 w-6 text-primary/40" />
					</div>
				{/if}
			{/each}
		</div>
	</div>
</section>
