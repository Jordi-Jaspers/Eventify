<script lang="ts">
	import Button from '$lib/components/ui/button/button.svelte';
	import { Lock, Unlock, Key } from '@lucide/svelte';
	import type { UserDetailsResponse } from '$lib/api/models';

	interface Props {
		user: UserDetailsResponse;
		lockingUser: boolean;
		forcingPasswordReset: boolean;
		onLockToggle: (userId: number | undefined, lock: boolean) => void;
		onForcePasswordReset: (userId: number | undefined, email: string | undefined) => void;
		onClose: () => void;
	}

	let { user, lockingUser, forcingPasswordReset, onLockToggle, onForcePasswordReset, onClose }: Props = $props();
</script>

<div class="sticky bottom-0 mt-4 p-4 border-t border-border/50 bg-background/98 backdrop-blur-xl shadow-lg">
	<div class="flex gap-2">
		<Button
			variant={user.enabled ? 'destructive' : 'default'}
			class="flex-1"
			onclick={() => onLockToggle(user?.id, !user?.enabled)}
			disabled={lockingUser}
		>
			{#if user.enabled}
				<Lock class="mr-2 h-4 w-4" />
				Lock User
			{:else}
				<Unlock class="mr-2 h-4 w-4" />
				Unlock User
			{/if}
		</Button>
		<Button
			variant="outline"
			onclick={() => onForcePasswordReset(user?.id, user?.email)}
			disabled={forcingPasswordReset}
		>
			<Key class="mr-2 h-4 w-4" />
			Reset Password
		</Button>
		<Button variant="outline" onclick={onClose}>
			Close
		</Button>
	</div>
</div>
