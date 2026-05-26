<script lang="ts">
	import { CLIENT_ROUTES } from '$lib/config/routes';
	import Button from '$lib/components/ui/button/button.svelte';
	import PublicNavbar from '$lib/components/layout/PublicNavbar.svelte';
	import PublicFooter from '$lib/components/layout/PublicFooter.svelte';
	import { HeroSection, FeaturesSection, CodeDemoSection } from '$lib/components/landing';

	function scrollToSection(sectionId: string): void {
		const element: HTMLElement | null = document.getElementById(sectionId);
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}
</script>

<svelte:head>
	<title>Eventify - Real-Time Event Monitoring for Modern Teams</title>
	<meta name="description" content="Track, analyze, and act on events across your distributed systems. Get instant insights with WebSocket-powered live updates." />
</svelte:head>

<PublicNavbar activePage="landing" />

<main class="overflow-x-hidden">
	<HeroSection onScrollToFeatures={() => scrollToSection('features')} />
	<FeaturesSection />
	<CodeDemoSection />

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

<PublicFooter
	productLinks={[
		{ label: 'Features', onclick: () => scrollToSection('features') },
		{ label: 'How It Works', onclick: () => scrollToSection('how-it-works') }
	]}
/>
