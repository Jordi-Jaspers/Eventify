<script lang="ts">
	import { validatePassword, type PasswordValidationResult, type PasswordRule } from '$lib/utils/password-validator';
	import { Badge } from '$lib/components/ui/badge';
	import { Check, X } from '@lucide/svelte';

	interface Props {
		password: string;
		class?: string;
	}

	let { password = '', class: className = '' }: Props = $props();

	const validation: PasswordValidationResult = $derived(validatePassword(password));
	const percentage: number = $derived((validation.score / validation.maxScore) * 100);

	// Gradient color based on strength
	const strengthGradient: string = $derived(
		validation.strength === 'weak'
			? 'bg-gradient-to-r from-red-600 to-red-400' :
		validation.strength === 'fair'
			? 'bg-gradient-to-r from-orange-600 to-orange-400' :
		validation.strength === 'good'
			? 'bg-gradient-to-r from-yellow-600 to-yellow-400' :
		'bg-gradient-to-r from-green-600 to-green-400'
	);

	const strengthLabel: string = $derived(
		validation.strength === 'weak' ? 'Weak' :
		validation.strength === 'fair' ? 'Fair' :
		validation.strength === 'good' ? 'Good' :
		'Strong'
	);

	const badgeVariant = $derived(
		validation.isValid ? 'default' : 'secondary'
	);

	// Glow effect when strong
	const glowClass: string = $derived(
		validation.strength === 'strong' ? 'animate-glow-pulse' : ''
	);
</script>

<div class={`space-y-3 ${className}`}>
	{#if password.length > 0}
		<!-- Strength Indicator -->
		<div class="space-y-2 animate-fade-in">
			<div class="flex items-center justify-between text-sm">
				<span class="text-muted-foreground">Password Strength</span>
				<Badge variant={badgeVariant} class="transition-all duration-300">
					{strengthLabel}
				</Badge>
			</div>

			<!-- Gradient Progress Bar -->
			<div class="relative h-2 bg-secondary/50 rounded-full overflow-hidden backdrop-blur-sm">
				<div
					class={`absolute inset-y-0 left-0 rounded-full transition-all duration-500 ease-out ${strengthGradient} ${glowClass}`}
					style={`width: ${percentage}%`}
					role="progressbar"
					aria-valuenow={percentage}
					aria-valuemin="0"
					aria-valuemax="100"
					aria-label="Password strength"
				></div>

				<!-- Shimmer effect for strong passwords -->
				{#if validation.strength === 'strong'}
					<div
						class="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent animate-shimmer"
						style="background-size: 1000px 100%;"
					></div>
				{/if}
			</div>
		</div>

		<!-- Rule Checklist -->
		<div class="space-y-1.5">
			{#each validation.rules as rule (rule.id)}
				<div class="flex items-center gap-2 text-sm transition-all duration-300">
					{#if rule.satisfied}
						<div class="flex-shrink-0 w-4 h-4 rounded-full bg-gradient-to-br from-green-500 to-green-600 flex items-center justify-center shadow-lg shadow-green-500/20">
							<Check class="h-3 w-3 text-white" />
						</div>
					{:else}
						<div class="flex-shrink-0 w-4 h-4 rounded-full bg-secondary flex items-center justify-center">
							<X class="h-3 w-3 text-muted-foreground" />
						</div>
					{/if}
					<span
						class="transition-all duration-300"
						class:text-green-400={rule.satisfied}
						class:font-medium={rule.satisfied}
						class:text-muted-foreground={!rule.satisfied}
					>
						{rule.label}
					</span>
				</div>
			{/each}
		</div>
	{/if}
</div>
