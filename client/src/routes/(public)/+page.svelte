<script lang="ts">
	import { isAuthenticated } from '$lib/stores/auth';
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import AppLogo from '$lib/components/layout/AppLogo.svelte';
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
		Menu,
		X,
		Copy,
		Check,
		ChevronDown
	} from '@lucide/svelte';
	import { onMount } from 'svelte';
	import { Card } from '$lib/components/ui/card';

	// Mobile menu state
	let mobileMenuOpen: boolean = $state(false);
	
	// Scroll state for navbar styling
	let scrolled: boolean = $state(false);
	
	// Copy button state
	let copied: boolean = $state(false);

	// Smooth scroll to section
	function scrollToSection(sectionId: string): void {
		const element: HTMLElement | null = document.getElementById(sectionId);
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
			mobileMenuOpen = false;
		}
	}

	// Copy code to clipboard
	async function copyCode(): Promise<void> {
		const code: string = `curl -X POST https://api.eventify.io/v1/events \\
  -H "Authorization: Bearer YOUR_API_KEY" \\
  -H "Content-Type: application/json" \\
  -d '{
    "channel": "payments",
    "type": "payment.completed",
    "data": {"amount": 99.99, "currency": "USD"}
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

	onMount(() => {
		const handleScroll = (): void => {
			scrolled = window.scrollY > 20;
		};

		window.addEventListener('scroll', handleScroll);
		return () => window.removeEventListener('scroll', handleScroll);
	});
</script>

<svelte:head>
	<title>Eventify - Real-Time Event Monitoring for Modern Teams</title>
	<meta name="description" content="Track, analyze, and act on events across your distributed systems. Get instant insights with WebSocket-powered live updates." />
</svelte:head>

<!-- Navigation Bar -->
<nav
	class="fixed top-0 left-0 right-0 z-50 transition-all duration-300 {scrolled
		? 'bg-card/90 backdrop-blur-xl border-b border-border/50 shadow-xl'
		: 'bg-transparent'}"
>
	<div class="container mx-auto px-4 sm:px-6 lg:px-8">
		<div class="flex items-center justify-between h-16">
			<!-- Logo -->
			<a href={CLIENT_ROUTES.LANDING_PAGE.path} class="flex items-center gap-2">
				<div
					class="inline-flex items-center justify-center w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-accent"
				>
					<Activity class="w-4 h-4 text-primary-foreground" />
				</div>
				<span class="text-xl font-bold gradient-text">Eventify</span>
			</a>

			<!-- Desktop Navigation -->
			<div class="hidden md:flex items-center gap-8">
				<button
					onclick={() => scrollToSection('features')}
					class="text-sm font-medium text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md px-2 py-1"
				>
					Features
				</button>
				<button
					onclick={() => scrollToSection('how-it-works')}
					class="text-sm font-medium text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md px-2 py-1"
				>
					How It Works
				</button>
				<button
					onclick={() => scrollToSection('cta')}
					class="text-sm font-medium text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md px-2 py-1"
				>
					Get Started
				</button>
			</div>

			<!-- Desktop Auth Buttons -->
			<div class="hidden md:flex items-center gap-3">
				{#if $isAuthenticated}
					<Button href={CLIENT_ROUTES.DASHBOARD_PAGE.path} class="bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all">
						Dashboard
					</Button>
				{:else}
					<Button href={CLIENT_ROUTES.LOGIN_PAGE.path} variant="outline">
						Login
					</Button>
					<Button href={CLIENT_ROUTES.REGISTER_PAGE.path} class="bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all">
						Sign Up Free
					</Button>
				{/if}
			</div>

			<!-- Mobile Menu Button -->
			<button
				onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
				class="md:hidden p-2 text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-md"
				aria-label="Toggle menu"
				aria-expanded={mobileMenuOpen}
			>
				{#if mobileMenuOpen}
					<X class="w-6 h-6" />
				{:else}
					<Menu class="w-6 h-6" />
				{/if}
			</button>
		</div>

	</div>
</nav>

<!-- Full-screen Mobile Menu -->
{#if mobileMenuOpen}
	<div class="fixed inset-0 z-40 md:hidden">
		<!-- Backdrop -->
		<button
			onclick={() => (mobileMenuOpen = false)}
			class="absolute inset-0 bg-background/80 backdrop-blur-sm"
			aria-label="Close menu"
		></button>
		
		<!-- Menu Panel -->
		<div class="absolute inset-x-0 top-16 bottom-0 bg-background/95 backdrop-blur-xl border-t border-border/50 animate-fade-in-up overflow-y-auto">
			<div class="flex flex-col h-full px-6 py-8">
				<!-- Navigation Links -->
				<nav class="flex-1 space-y-2">
					<button
						onclick={() => scrollToSection('features')}
						class="flex items-center w-full px-4 py-4 text-lg font-medium text-foreground hover:text-primary hover:bg-accent/10 rounded-xl transition-colors"
					>
						Features
					</button>
					<button
						onclick={() => scrollToSection('how-it-works')}
						class="flex items-center w-full px-4 py-4 text-lg font-medium text-foreground hover:text-primary hover:bg-accent/10 rounded-xl transition-colors"
					>
						How It Works
					</button>
					<button
						onclick={() => scrollToSection('cta')}
						class="flex items-center w-full px-4 py-4 text-lg font-medium text-foreground hover:text-primary hover:bg-accent/10 rounded-xl transition-colors"
					>
						Get Started
					</button>
				</nav>
				
				<!-- Auth Buttons at Bottom -->
				<div class="pt-6 border-t border-border/50 space-y-3">
					{#if $isAuthenticated}
						<Button href={CLIENT_ROUTES.DASHBOARD_PAGE.path} class="w-full h-12 text-base bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all">
							Dashboard
						</Button>
					{:else}
						<Button href={CLIENT_ROUTES.LOGIN_PAGE.path} variant="outline" class="w-full h-12 text-base">
							Login
						</Button>
						<Button href={CLIENT_ROUTES.REGISTER_PAGE.path} class="w-full h-12 text-base bg-gradient-to-r from-primary to-accent hover:opacity-90 transition-all">
							Sign Up Free
						</Button>
					{/if}
				</div>
			</div>
		</div>
	</div>
{/if}

<!-- Main Content -->
<main>
	<!-- Hero Section -->
	<section id="hero" class="pt-32 pb-20 px-4 sm:px-6 lg:px-8">
		<div class="container mx-auto max-w-7xl">
			<div class="grid lg:grid-cols-2 gap-12 items-center">
				<!-- Hero Text -->
				<div class="animate-fade-in-up space-y-6">
					<h1 class="text-4xl sm:text-5xl lg:text-6xl font-bold leading-tight">
						<span class="gradient-text-animated">Real-Time Event Monitoring</span>
						<br class="block" />
						<span class="text-foreground relative z-10">for Modern Teams</span>
					</h1>
					<p class="text-lg sm:text-xl text-muted-foreground">
						Track, analyze, and act on events across your distributed systems. Get instant insights
						with WebSocket-powered live updates.
					</p>
					<div class="flex flex-col sm:flex-row gap-4 pt-4">
						<Button
							href={CLIENT_ROUTES.REGISTER_PAGE.path}
							size="lg"
							class="text-base bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all shadow-lg hover:shadow-primary/50"
						>
							Get Started Free
						</Button>
						<Button
							onclick={() => scrollToSection('features')}
							size="lg"
							variant="outline"
							class="text-base"
						>
							Learn More
							<ChevronDown class="ml-2 h-4 w-4" />
						</Button>
					</div>
				</div>

				<!-- Hero Visual - Animated Dashboard Mockup -->
				<div class="relative animate-fade-in">
					<!-- Main Dashboard -->
					<div
						class="rounded-xl border border-border/50 bg-card/60 backdrop-blur-xl p-6 shadow-2xl"
					>
						<div class="space-y-4">
							<!-- Header -->
							<div class="flex items-center justify-between pb-4 border-b border-border/50">
								<div class="flex items-center gap-2">
									<Activity class="h-5 w-5 text-primary" />
									<span class="font-semibold">Live Events</span>
								</div>
								<div class="flex items-center gap-2">
									<span class="relative flex h-3 w-3">
										<span
											class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"
										></span>
										<span class="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
									</span>
									<span class="text-xs text-muted-foreground">Connected</span>
								</div>
							</div>

							<!-- Event Cards -->
							<div class="space-y-3">
								<div class="animate-fade-in-up rounded-lg border border-green-500/20 bg-green-500/10 p-3">
									<div class="flex items-center gap-2">
										<span class="text-green-500">●</span>
										<span class="text-sm font-mono">user.login</span>
									</div>
									<p class="text-xs text-muted-foreground mt-1">User authenticated successfully</p>
								</div>

								<div
									class="animate-fade-in-up rounded-lg border border-blue-500/20 bg-blue-500/10 p-3"
									style="animation-delay: 0.2s;"
								>
									<div class="flex items-center gap-2">
										<span class="text-blue-500">●</span>
										<span class="text-sm font-mono">payment.success</span>
									</div>
									<p class="text-xs text-muted-foreground mt-1">Payment processed - $99.99 USD</p>
								</div>

								<div
									class="animate-fade-in-up rounded-lg border border-amber-500/20 bg-amber-500/10 p-3"
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
						class="absolute -top-4 -right-4 max-w-[200px] animate-fade-in-up rounded-lg border border-border/50 bg-card backdrop-blur-xl p-3 shadow-xl"
						style="animation-delay: 0.6s;"
					>
						<div class="flex items-center gap-2">
							<Shield class="h-4 w-4 text-primary" />
							<span class="text-xs font-medium">Secure & Encrypted</span>
						</div>
					</div>

					<div
						class="absolute -bottom-4 -left-4 max-w-[180px] animate-fade-in-up rounded-lg border border-border/50 bg-card backdrop-blur-xl p-3 shadow-xl"
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
				<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold mb-4">
					<span class="gradient-text">Powerful Features</span>
				</h2>
				<p class="text-lg text-muted-foreground max-w-2xl mx-auto">
					Everything you need to monitor, analyze, and respond to events in real-time
				</p>
			</div>

			<div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
				<!-- Feature 1: Real-Time Streaming -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<Activity class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Real-Time Streaming</h3>
					<p class="text-muted-foreground">
						WebSocket-powered live updates deliver events the moment they happen. No polling, no
						delays.
					</p>
				</div>

				<!-- Feature 2: Multi-Organization Support -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: 0.1s;"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<Building2 class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Multi-Organization Support</h3>
					<p class="text-muted-foreground">
						Manage multiple organizations with isolated data, separate API keys, and custom retention
						policies.
					</p>
				</div>

				<!-- Feature 3: Channel-Based Routing -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: 0.2s;"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<GitBranch class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Channel-Based Routing</h3>
					<p class="text-muted-foreground">
						Route events to specific channels for organized monitoring. Filter and search with
						precision.
					</p>
				</div>

				<!-- Feature 4: Developer-First API -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: 0.3s;"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<Code2 class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Developer-First API</h3>
					<p class="text-muted-foreground">
						RESTful API with comprehensive documentation. Integrate in minutes with any language or
						framework.
					</p>
				</div>

				<!-- Feature 5: Team Collaboration -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: 0.4s;"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<Users class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Team Collaboration</h3>
					<p class="text-muted-foreground">
						Role-based access control, team dashboards, and shared insights. Keep everyone in sync.
					</p>
				</div>

				<!-- Feature 6: Enterprise Security -->
				<div
					class="group rounded-xl border border-border/60 bg-card/50 backdrop-blur-xl shadow-xl p-6 transition-all hover:border-primary/50 hover:shadow-2xl hover:shadow-primary/10 animate-fade-in-up"
					style="animation-delay: 0.5s;"
				>
					<div
						class="mb-4 inline-flex rounded-lg bg-gradient-to-br from-primary/20 to-primary/5 p-3"
					>
						<Shield class="h-6 w-6 text-primary" />
					</div>
					<h3 class="text-xl font-semibold mb-2">Enterprise Security</h3>
					<p class="text-muted-foreground">
						API key rotation, audit logs, rate limiting, and encrypted storage. Built for compliance.
					</p>
				</div>
			</div>
		</div>
	</section>

	<!-- How It Works Section -->
	<section id="how-it-works" class="py-20 px-4 sm:px-6 lg:px-8">
		<div class="container mx-auto max-w-7xl">
			<div class="text-center mb-16 animate-fade-in-up">
				<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold mb-4">
					<span class="gradient-text">How It Works</span>
				</h2>
				<p class="text-lg text-muted-foreground max-w-2xl mx-auto">
					Get started in three simple steps
				</p>
			</div>

			<div class="grid md:grid-cols-3 gap-8 lg:gap-12">
				<!-- Step 1 -->
				<div class="relative animate-fade-in-up">
					<div class="text-center bg-card/50 backdrop-blur-xl border border-border/50 shadow-lg p-8 rounded-xl">
						<div
							class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 border border-primary/30 mb-6"
						>
							<Plug class="h-8 w-8 text-primary" />
						</div>
						<div
							class="inline-flex items-center justify-center bg-primary/20 text-primary rounded-full w-8 h-8 flex items-center justify-center font-semibold mb-4 mx-auto"
						>
							1
						</div>
						<h3 class="text-xl font-semibold mb-3">Connect</h3>
						<p class="text-muted-foreground">
							Integrate with a simple API call. Works with any language or platform.
						</p>
					</div>
					<!-- Connector Line (hidden on mobile) -->
					<div
						class="hidden md:block absolute top-8 left-1/2 w-full h-0.5 bg-gradient-to-r from-primary/50 to-transparent"
					></div>
				</div>

				<!-- Step 2 -->
				<div class="relative animate-fade-in-up" style="animation-delay: 0.2s;">
					<div class="text-center bg-card/50 backdrop-blur-xl border border-border/50 shadow-lg p-8 rounded-xl">
						<div
							class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 border border-primary/30 mb-6"
						>
							<Radio class="h-8 w-8 text-primary" />
						</div>
						<div
							class="inline-flex items-center justify-center bg-primary/20 text-primary rounded-full w-8 h-8 flex items-center justify-center font-semibold mb-4 mx-auto"
						>
							2
						</div>
						<h3 class="text-xl font-semibold mb-3">Stream</h3>
						<p class="text-muted-foreground">
							Events flow in real-time. Filter, search, and monitor as they happen.
						</p>
					</div>
					<!-- Connector Line (hidden on mobile) -->
					<div
						class="hidden md:block absolute top-8 left-1/2 w-full h-0.5 bg-gradient-to-r from-primary/50 to-transparent"
					></div>
				</div>

				<!-- Step 3 -->
				<div class="animate-fade-in-up" style="animation-delay: 0.4s;">
					<div class="text-center bg-card/50 backdrop-blur-xl border border-border/50 shadow-lg p-8 rounded-xl">
						<div
							class="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 border border-primary/30 mb-6"
						>
							<BarChart3 class="h-8 w-8 text-primary" />
						</div>
						<div
							class="inline-flex items-center justify-center bg-primary/20 text-primary rounded-full w-8 h-8 flex items-center justify-center font-semibold mb-4 mx-auto"
						>
							3
						</div>
						<h3 class="text-xl font-semibold mb-3">Analyze</h3>
						<p class="text-muted-foreground">
							Gain insights with dashboards, alerts, and trend visualization.
						</p>
					</div>
				</div>
			</div>
		</div>
	</section>

	<!-- Code Example Section -->
	<section class="py-20 px-4 sm:px-6 lg:px-8 bg-muted/30">
		<div class="container mx-auto max-w-4xl">
			<div class="text-center mb-12 animate-fade-in-up">
				<h2 class="text-3xl sm:text-4xl font-bold mb-4">
					<span class="gradient-text">Simple Integration</span>
				</h2>
				<p class="text-lg text-muted-foreground">
					Start sending events with a single API call
				</p>
			</div>

			<div class="animate-fade-in-up">
				<Card class="bg-card/50 backdrop-blur-xl border border-border/50 shadow-lg overflow-hidden">
					<!-- Code Header -->
					<div class="flex items-center justify-between px-6 py-4 border-b border-border/50 bg-muted/30">
						<div class="flex items-center gap-2">
							<Code2 class="h-4 w-4 text-primary" />
							<span class="text-sm font-medium">cURL</span>
						</div>
						<button
							onclick={copyCode}
							class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium bg-primary/10 hover:bg-primary/20 text-primary transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
							aria-label="Copy code"
						>
							{#if copied}
								<Check class="h-3 w-3" />
								Copied!
							{:else}
								<Copy class="h-3 w-3" />
								Copy
							{/if}
						</button>
					</div>

					<!-- Code Block -->
					<div class="p-6 overflow-x-auto">
						<pre class="text-sm"><code class="language-bash text-muted-foreground"><span class="text-primary">curl</span> <span class="text-amber-500">-X POST</span> <span class="text-green-500">https://api.eventify.io/v1/events</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"Authorization: Bearer YOUR_API_KEY"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-H</span> <span class="text-green-500">"Content-Type: application/json"</span> <span class="text-amber-500">\</span>
  <span class="text-amber-500">-d</span> <span class="text-green-500">'{`{
    "channel": "payments",
    "type": "payment.completed",
    "data": {"amount": 99.99, "currency": "USD"}
  }`}'</span></code></pre>
					</div>
				</Card>

				<!-- Documentation Link -->
				<div class="text-center mt-8">
					<p class="text-sm text-muted-foreground">
						Want to learn more?
						<button class="text-primary hover:underline font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 rounded-sm px-1">
							See the documentation →
						</button>
					</p>
				</div>
			</div>
		</div>
	</section>

	<!-- Final CTA Section -->
	<section id="cta" class="py-20 px-4 sm:px-6 lg:px-8">
		<div class="container mx-auto max-w-4xl text-center">
			<div class="animate-fade-in-up space-y-8">
				<h2 class="text-3xl sm:text-4xl lg:text-5xl font-bold">
					<span class="gradient-text-animated">Start Monitoring in Minutes</span>
				</h2>
				<p class="text-lg sm:text-xl text-muted-foreground max-w-2xl mx-auto">
					Free to get started. No credit card required.
				</p>
				<div class="pt-4">
					<Button
						href={CLIENT_ROUTES.REGISTER_PAGE.path}
						size="lg"
						class="text-lg px-8 py-6 bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70 transition-all shadow-lg hover:shadow-primary/50"
					>
						Create Free Account
					</Button>
				</div>
			</div>
		</div>
	</section>
</main>

<!-- Footer -->
<footer class="border-t border-border/50 bg-muted/30 py-12 px-4 sm:px-6 lg:px-8">
	<div class="container mx-auto max-w-7xl">
		<div class="grid md:grid-cols-4 gap-8 mb-8">
			<!-- Brand -->
			<div class="md:col-span-2">
				<div class="flex items-center gap-2 mb-4">
					<div
						class="inline-flex items-center justify-center w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-accent"
					>
						<Activity class="w-4 h-4 text-primary-foreground" />
					</div>
					<span class="text-lg font-bold gradient-text">Eventify</span>
				</div>
				<p class="text-sm text-muted-foreground max-w-sm">
					Real-time event monitoring for modern teams. Track, analyze, and act on events across
					your distributed systems.
				</p>
			</div>

			<!-- Product Links -->
			<div>
				<h4 class="font-semibold mb-4">Product</h4>
				<ul class="space-y-2 text-sm">
					<li>
						<button
							onclick={() => scrollToSection('features')}
							class="text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:underline"
						>
							Features
						</button>
					</li>
					<li>
						<button
							onclick={() => scrollToSection('how-it-works')}
							class="text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:underline"
						>
							How It Works
						</button>
					</li>
				</ul>
			</div>

			<!-- Account Links -->
			<div>
				<h4 class="font-semibold mb-4">Account</h4>
				<ul class="space-y-2 text-sm">
					<li>
						<a href={CLIENT_ROUTES.LOGIN_PAGE.path} class="text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:underline">
							Login
						</a>
					</li>
					<li>
						<a href={CLIENT_ROUTES.REGISTER_PAGE.path} class="text-muted-foreground hover:text-primary transition-colors focus-visible:outline-none focus-visible:underline">
							Register
						</a>
					</li>
				</ul>
			</div>
		</div>

		<!-- Copyright -->
		<div class="pt-8 border-t border-border/50">
			<p class="text-sm text-center text-muted-foreground">
				© {new Date().getFullYear()} Eventify. All rights reserved.
			</p>
		</div>
	</div>
</footer>
