<script lang="ts">
	import { Label } from '$lib/components/ui/label';

	function calculatePasswordStrength(password: string): { score: number; missing: string[] } {
		let score = 0;
		let missing = [];

		const lengthRule = password.length >= 8;
		const tooLongRule = password.length <= 100;
		const upperCaseRule = /[A-Z]/.test(password);
		const lowerCaseRule = /[a-z]/.test(password);
		const digitRule = /[0-9]/.test(password);
		const specialCharRule = /[^A-Za-z0-9]/.test(password);
		const whitespaceRule = !/\s/.test(password);

		if (lengthRule) score += 1;
		else missing.push('At least 8 characters');
		if (tooLongRule) score += 1;
		else missing.push('At most 100 characters');
		if (upperCaseRule) score += 1;
		else missing.push('At least one upper-case character');
		if (lowerCaseRule) score += 1;
		else missing.push('At least one lower-case character');
		if (digitRule) score += 1;
		else missing.push('At least one digit character');
		if (specialCharRule) score += 1;
		else missing.push('At least one special character');
		if (whitespaceRule) score += 1;
		else missing.push('No whitespace allowed');

		return { score, missing };
	}

	function getPasswordStrengthLabel(score: number): string {
		switch (score) {
			case 7:
				return 'Very Strong';
			case 6:
				return 'Strong';
			case 5:
				return 'Medium';
			case 4:
				return 'Weak';
			default:
				return 'Very Weak';
		}
	}

	let {
		password,
		confirmation,
		isValid = $bindable()
	} = $props<{
		password: string;
		confirmation: string;
		isValid: boolean;
	}>();
	let { score, missing } = $derived.by(() => calculatePasswordStrength(password || ''));
	let isMatch = $derived(password === confirmation && password !== '');

	let pwProps = $derived.by(() => {
		const passwordStrengthProps = {
			strength: score,
			label: getPasswordStrengthLabel(score),
			bgColor: score >= 7 ? 'bg-green-500' : score >= 4 ? 'bg-orange-500' : 'bg-red-500',
			textColor: score >= 7 ? 'text-green-500' : score >= 4 ? 'text-orange-500' : 'text-red-500',
			missingRequirements: [...missing]
		};

		if (!isMatch && password && confirmation) {
			passwordStrengthProps.missingRequirements.push('Password should match confirmation');
		}

		return passwordStrengthProps;
	});

	$effect(() => {
		isValid = score === 7 && isMatch;
	});
</script>

{#if password !== ''}
	<div>
		<Label>Password Strength: <span class={pwProps.textColor}>{pwProps.label}</span></Label>
		<div class="mt-1 h-2 rounded bg-gray-200">
			<div class="h-full rounded transition-all duration-300 {pwProps.bgColor}" style="width: {(pwProps.strength * 100) / 7}%"></div>
		</div>
		{#if pwProps.missingRequirements.length > 0}
			<div class="mt-2 text-sm text-red-500">
				<ul class="list-inside list-disc">
					{#each pwProps.missingRequirements as requirement}
						<li>{requirement}</li>
					{/each}
				</ul>
			</div>
		{/if}
	</div>
{/if}
