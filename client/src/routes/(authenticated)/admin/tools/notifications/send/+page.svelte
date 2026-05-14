<script lang="ts">
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { Label } from '$lib/components/ui/label';
	import * as Select from '$lib/components/ui/select';
	import * as Card from '$lib/components/ui/card';
	import { Badge } from '$lib/components/ui/badge';
	import { Textarea } from '$lib/components/ui/textarea';
	import { LoaderCircle, Send, Users, ExternalLink, ArrowRight } from '@lucide/svelte';
	import UserSearchCombobox from '$lib/components/user/UserSearchCombobox.svelte';
	import * as AlertDialog from '$lib/components/ui/alert-dialog';
	import { type BroadcastCategory, type AudienceType } from '$lib/api/models';
	import { createBroadcastSendService } from '$lib/api/admin/service/BroadcastSendService.svelte';

	const svc = createBroadcastSendService();

	const categoryOptions: { value: BroadcastCategory; label: string }[] = [
		{ value: 'ANNOUNCEMENT', label: 'Announcement' },
		{ value: 'SYSTEM', label: 'System' },
		{ value: 'ALERT', label: 'Alert' },
		{ value: 'UPDATE', label: 'Update' }
	];

	const audienceOptions: { value: AudienceType; label: string }[] = [
		{ value: 'ALL_USERS', label: 'All Users' },
		{ value: 'ALL_ORGANIZATION_OWNERS', label: 'All Organization Owners' },
		{ value: 'ORGANIZATION', label: 'Specific Organization' },
		{ value: 'USER', label: 'Specific User' },
		{ value: 'GLOBAL_ROLE', label: 'Global Role' }
	];
</script>

<div class="space-y-6">
	<Card.Root class="bg-card/50 backdrop-blur-xl border-border/50">
		<Card.Header>
			<Card.Title class="flex items-center gap-2">
				<Send class="h-5 w-5 text-primary" />
				Compose Broadcast
			</Card.Title>
		</Card.Header>
		<Card.Content class="space-y-6">
			<!-- Category -->
			<div class="space-y-2">
				<Label for="category">Category</Label>
				<Select.Root
					type="single"
					value={svc.category}
					onValueChange={(v) => {
						if (v) svc.category = v as BroadcastCategory;
					}}
				>
					<Select.Trigger id="category" class="bg-background/50 border-border/50">
						{categoryOptions.find((o) => o.value === svc.category)?.label ?? 'Select category'}
					</Select.Trigger>
					<Select.Content>
						{#each categoryOptions as opt (opt.value)}
							<Select.Item value={opt.value}>{opt.label}</Select.Item>
						{/each}
					</Select.Content>
				</Select.Root>
			</div>

			<!-- Title -->
			<div class="space-y-2">
				<div class="flex justify-between">
					<Label for="title">Title</Label>
					<span class="text-xs text-muted-foreground">{svc.title.length}/120</span>
				</div>
				<Input
					id="title"
					bind:value={svc.title}
					maxlength={120}
					placeholder="Notification title..."
					class="bg-background/50 border-border/50"
				/>
			</div>

			<!-- Message -->
			<div class="space-y-2">
				<div class="flex justify-between">
					<Label for="message">Message</Label>
					<span class="text-xs text-muted-foreground">{svc.message.length}/500</span>
				</div>
				<Textarea
					id="message"
					bind:value={svc.message}
					maxlength={500}
					placeholder="Notification message..."
					rows={4}
					class="bg-background/50 border-border/50 resize-none"
				/>
			</div>

			<!-- Audience -->
			<div class="space-y-3">
				<Label>Audience</Label>
				<Select.Root
					type="single"
					value={svc.audienceType}
					onValueChange={(v) => {
						if (v) svc.audienceType = v as AudienceType;
					}}
				>
					<Select.Trigger class="bg-background/50 border-border/50">
						{audienceOptions.find((o) => o.value === svc.audienceType)?.label ?? 'Select audience'}
					</Select.Trigger>
					<Select.Content>
						{#each audienceOptions as opt (opt.value)}
							<Select.Item value={opt.value}>{opt.label}</Select.Item>
						{/each}
					</Select.Content>
				</Select.Root>

				{#if svc.audienceType === 'USER'}
					<UserSearchCombobox
						onSelect={(u) => { svc.selectedUser = u; }}
						selectedUser={svc.selectedUser}
						placeholder="Search for a user..."
					/>
				{:else if svc.audienceType === 'ORGANIZATION'}
					<div class="relative">
						{#if svc.selectedOrg}
							<div
								class="flex items-center justify-between p-3 rounded-lg border border-border/50 bg-background/50"
							>
								<span class="text-sm font-medium">{svc.selectedOrg.name}</span>
								<Button variant="ghost" size="sm" onclick={svc.clearOrg} class="h-7 w-7 p-0">
									✕
								</Button>
							</div>
						{:else}
							<Input
								bind:value={svc.orgQuery}
								placeholder="Search organizations..."
								class="bg-background/50 border-border/50"
							/>
							{#if svc.showOrgDropdown}
								<div
									class="absolute z-50 w-full mt-1 rounded-lg border border-border/50 bg-card/95 backdrop-blur-xl shadow-2xl overflow-hidden"
								>
									{#if svc.orgSearching}
										<div class="p-3 flex items-center gap-2 text-sm text-muted-foreground">
											<LoaderCircle class="h-4 w-4 animate-spin" /> Searching...
										</div>
									{:else if svc.orgResults.length === 0}
										<div class="p-3 text-sm text-muted-foreground">No organizations found</div>
									{:else}
										{#each svc.orgResults as org (org.id)}
											<button
												type="button"
												onclick={() => svc.selectOrg(org)}
												class="w-full p-3 text-left text-sm hover:bg-accent/10 transition-colors border-b border-border/30 last:border-0"
											>
												{org.name}
											</button>
										{/each}
									{/if}
								</div>
							{/if}
						{/if}
					</div>
				{:else if svc.audienceType === 'GLOBAL_ROLE'}
					<div class="p-3 rounded-lg border border-border/50 bg-background/50 text-sm">
						Role: <Badge variant="secondary">ADMIN</Badge>
					</div>
				{/if}

				<!-- Recipient count badge -->
				<div class="flex items-center gap-2">
					<Users class="h-4 w-4 text-muted-foreground" />
					{#if svc.previewLoading}
						<span class="text-sm text-muted-foreground flex items-center gap-1">
							<LoaderCircle class="h-3 w-3 animate-spin" /> Calculating...
						</span>
					{:else if svc.recipientCount !== null}
						<Badge variant={svc.recipientCount > 100 ? 'destructive' : 'secondary'}>
							{svc.recipientCount} recipient{svc.recipientCount !== 1 ? 's' : ''}
						</Badge>
					{:else}
						<span class="text-sm text-muted-foreground">Select audience to preview count</span>
					{/if}
				</div>
			</div>

			<!-- Action URL + Label -->
			<div class="grid grid-cols-2 gap-4">
				<div class="space-y-2">
					<Label for="actionUrl">Action URL <span class="text-muted-foreground">(optional)</span></Label>
					<Input
						id="actionUrl"
						bind:value={svc.actionUrl}
						placeholder="https://..."
						class="bg-background/50 border-border/50"
					/>
				</div>
				<div class="space-y-2">
					<div class="flex justify-between">
						<Label for="actionLabel">Action Label <span class="text-muted-foreground">(optional)</span></Label>
						<span class="text-xs text-muted-foreground">{svc.actionLabel.length}/40</span>
					</div>
					<Input
						id="actionLabel"
						bind:value={svc.actionLabel}
						maxlength={40}
						placeholder="View details"
						class="bg-background/50 border-border/50"
					/>
				</div>
			</div>
		{#if !svc.isActionValid()}
			<p class="text-sm text-destructive">Action URL and Action Label must both be set or both empty.</p>
		{/if}
		{#if svc.hasActionUrl() && !svc.isUrlFormatValid()}
			<p class="text-sm text-destructive">URL must start with "/" (internal) or "http://" / "https://" (external).</p>
		{/if}
		{#if svc.hasActionUrl() && svc.isUrlFormatValid()}
			<div class="flex items-center gap-1.5 text-xs text-muted-foreground">
				{#if svc.isInternalUrl()}
					<ArrowRight class="h-3 w-3" />
					<span>Internal route — opens in same tab</span>
				{:else}
					<ExternalLink class="h-3 w-3" />
					<span>External link — opens in new tab</span>
				{/if}
			</div>
		{/if}
		</Card.Content>
		<Card.Footer>
			<Button onclick={svc.openConfirm} disabled={!svc.canSubmit} class="gap-2">
				{#if svc.sending}
					<LoaderCircle class="h-4 w-4 animate-spin" />
					Sending...
				{:else}
					<Send class="h-4 w-4" />
					Send Broadcast
				{/if}
			</Button>
		</Card.Footer>
	</Card.Root>
</div>

<AlertDialog.Root open={svc.confirmOpen} onOpenChange={(v) => (svc.confirmOpen = v)}>
	<AlertDialog.Portal>
		<AlertDialog.Overlay />
		<AlertDialog.Content class="bg-card/95 backdrop-blur-xl border-border/50">
			<AlertDialog.Header>
				<AlertDialog.Title>
					{svc.needsTypeConfirm ? `Send to ${svc.recipientCount} users?` : 'Send broadcast?'}
				</AlertDialog.Title>
				<AlertDialog.Description>
					{#if svc.needsTypeConfirm}
						<div class="space-y-3">
							<p>
								This will send to <strong>{svc.recipientCount}</strong> users. Type
								<strong>{svc.recipientCount}</strong> to confirm.
							</p>
							<Input
								bind:value={svc.confirmInput}
								placeholder={String(svc.recipientCount)}
								class="bg-background/50 border-border/50"
							/>
						</div>
					{:else}
						<p>This will send the notification to the selected audience.</p>
					{/if}
				</AlertDialog.Description>
			</AlertDialog.Header>
			<AlertDialog.Footer>
				<AlertDialog.Cancel>Cancel</AlertDialog.Cancel>
				<AlertDialog.Action onclick={svc.confirmSend} disabled={!svc.confirmReady}>
					Send
				</AlertDialog.Action>
			</AlertDialog.Footer>
		</AlertDialog.Content>
	</AlertDialog.Portal>
</AlertDialog.Root>
