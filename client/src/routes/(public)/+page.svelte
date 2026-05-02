<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import PublicNavbar from '$lib/components/layout/PublicNavbar.svelte';
	import PublicFooter from '$lib/components/layout/PublicFooter.svelte';
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
		Copy,
		Check,
		ChevronDown,
		ChevronRight
	} from '@lucide/svelte';
	import { type Component } from 'svelte';
	import { Card } from '$lib/components/ui/card';
	import { PulseIndicator } from '$lib/components/ui/pulse-indicator';

	// Copy button state
	let copied: boolean = $state(false);

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

	// Smooth scroll to section
	function scrollToSection(sectionId: string): void {
		const element: HTMLElement | null = document.getElementById(sectionId);
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}

	// Copy code to clipboard

	async function copyCode(): Promise<void> {
		const code: string = `curl -X POST https://api.eventify.io/v1/external/event \\
  -H "X-API-Key: ev_live_abc123" \\
  -H "Content-Type: application/json" \\
  -d '{
    "slug": "deployment",
    "severity": "info",
    "title": "Deployment successful",
    "message": "v2.1.0 deployed to production",
    "metadata": {"service": "api-gateway", "region": "eu-west-1"}
  }'`;

		try {
			await navigator.clipboard.writeText(code);
			copied = true;
			setTimeout(() => {
				copied = false;
			}, 2000);
	} catch (err: unknown) {
		console.error('Failed to copy:', err);
	}
}

</script>

<svelte:head>
	<title>Eventify - Real-Time Event Monitoring for Modern Teams</title>
	<meta name="description" content="Track, analyze, and act on events across your distributed systems. Get instant insights with WebSocket-powered live updates." />
</svelte:head>

<!-- Navigation Bar -->
<PublicNavbar activePage="landing" />

<!-- Main Content -->
<main class="overflow-x-hidden">
	<!-- Hero Section -->
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
							onclick={() => scrollToSection('features')}
							size="lg"
							variant="outline"
							class="text-base w-full sm:w-auto"
						>
							Learn More
							<ChevronDown class="ml-2 h-4 w-4" />
						</Button>
					</div>
				</div>

				<!-- Hero Visual - Animated Dashboard Mockup -->
				<div class="relative animate-fade-in mt-8 lg:mt-0">
					<!-- Main Dashboard -->
					<div
						class="rounded-lg border border-border/50 bg-card/60 backdrop-blur-xl p-6 shadow-xl"
					>
						<div class="space-y-4">
							<!-- Header -->
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

							<!-- Event Cards -->
							<div class="space-y-3">
								<div class="animate-fade-in-up rounded-md border border-green-500/20 bg-green-500/10 p-3">
									<div class="flex items-center gap-2">
										<span class="text-green-500">●</span>
										<span class="text-sm font-mono">user.login</span>
									</div>
									<p class="text-xs text-muted-foreground mt-1">User authenticated successfully</p>
								</div>

								<div
									class="animate-fade-in-up rounded-md border border-blue-500/20 bg-blue-500/10 p-3"
									style="animation-delay: 0.2s;"
								>
									<div class="flex items-center gap-2">
										<span class="text-blue-500">●</span>
										<span class="text-sm font-mono">payment.success</span>
									</div>
									<p class="text-xs text-muted-foreground mt-1">Payment processed - $99.99 USD</p>
								</div>

								<div
									class="animate-fade-in-up rounded-md border border-amber-500/20 bg-amber-500/10 p-3"
									style="animation-delay: 0.4s;"
								>
									<div class="flex items-center gap-2">
										<span class="text-amber-500">●</span>
										<span class="text-sm font-mono">api.rate_limit</span>
									</div>
									<p class="text-xs text-muted-foreground mt-1">Rate limit warning - 80% capacity</p>
								</div>
							</div>
						</div>
					</div>

					<!-- Floating Notification Cards -->
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

	<!-- Code Example Section -->
	<section class="py-20 px-4 sm:px-6 lg:px-8 bg-muted/30">
		<div class="container mx-auto max-w-4xl">
			<div class="text-center mb-12 animate-fade-in-up">
				<h2 class="text-3xl sm:text-4xl font-bold mb-4 text-foreground">
					Simple Integration
				</h2>
				<p class="text-lg text-muted-foreground">
					Start sending events with a single API call
				</p>
			</div>

			<div class="animate-fade-in-up">
				<Card class="bg-card/50 backdrop-blur-xl border border-border/50 shadow-md overflow-hidden">
					<!-- Code Block -->
					<div class="relative p-6 overflow-x-auto">
						<Button
							variant="ghost"
							size="sm"
							onclick={copyCode}
							class="absolute top-3 right-3 h-8 text-xs font-medium hover:bg-primary/20 hover:text-primary z-10"
							aria-label="Copy code"
						>
							{#if copied}
								<Check class="h-3 w-3 mr-1.5" />
								Copied!
							{:else}
								<Copy class="h-3 w-3 mr-1.5" />
								Copy
							{/if}
						</Button>
						<pre class="text-sm"><code class="language-bash text-muted-foreground"><span class="text-primary">curl</span> <span class="text-amber-500">-X POST</span> <span class="text-green-500">https://api.eventify.io/v1/external/event</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"X-API-Key: ev_live_abc123"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"Content-Type: application/json"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-d</span> <span class="text-green-500">'{`{
    "slug": "deployment",
    "severity": "info",
    "title": "Deployment successful",
    "message": "v2.1.0 deployed to production",
    "metadata": {"service": "api-gateway", "region": "eu-west-1"}
  }`}'</span></code></pre>
					</div>
				</Card>

				<!-- Documentation Link -->
				<div class="text-center mt-8">
					<p class="text-sm text-muted-foreground">
						Want to learn more?
						<Button variant="link" class="text-primary hover:underline px-1 h-auto font-medium">
							See the documentation →
						</Button>
					</p>
				</div>
			</div>
		</div>
	</section>

	<!-- Final CTA Section -->
	<section id="cta" class="py-20 px-4 sm:px-6 lg:px-8">
		<div class="container mx-auto max-w-4xl text-center">
			<div class="animate-fade-in-up space-y-8">
				<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold text-foreground">
					Start Monitoring in Minutes
				</h2>
				<p class="text-lg sm:text-xl text-muted-foreground max-w-2xl mx-auto">
					Free to get started. No credit card required.
				</p>
				<div class="pt-4">
					<Button
						href={CLIENT_ROUTES.REGISTER_PAGE.path}
						size="lg"
						class="text-lg px-8 py-6"
					>
						Create Free Account
					</Button>
				</div>
			</div>
		</div>
	</section>
</main>

<!-- Footer -->
<PublicFooter
	productLinks={[
		{ label: 'Features', onclick: () => scrollToSection('features') },
		{ label: 'How It Works', onclick: () => scrollToSection('how-it-works') }
	]}
/>
