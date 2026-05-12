import { toast } from 'svelte-sonner';
import { handleError } from '$lib/utils/error-handler';
import {
	sendBroadcast,
	previewRecipientCount,
	type BroadcastCategory,
	type AudienceType,
	type BroadcastAudience
} from '$lib/api/admin/AdminNotificationController';
import { searchOrganizations } from '$lib/api/organization/OrganizationController';
import type { UserResponse, OrganizationResponse } from '$lib/api/models';

export function createBroadcastSendService() {
	// Form state
	let category: BroadcastCategory = $state('ANNOUNCEMENT');
	let title: string = $state('');
	let message: string = $state('');
	let audienceType: AudienceType = $state('ALL_USERS');
	let selectedUser: UserResponse | undefined = $state(undefined);
	let selectedOrg: OrganizationResponse | undefined = $state(undefined);
	let actionUrl: string = $state('');
	let actionLabel: string = $state('');

	// UI state
	let sending: boolean = $state(false);
	let recipientCount: number | null = $state(null);
	let previewLoading: boolean = $state(false);
	let confirmOpen: boolean = $state(false);
	let confirmInput: string = $state('');

	// Org search state
	let orgQuery: string = $state('');
	let orgResults: OrganizationResponse[] = $state([]);
	let orgSearching: boolean = $state(false);
	let orgDebounceTimer: ReturnType<typeof setTimeout> | null = null;
	let showOrgDropdown: boolean = $state(false);

	function isAudienceValid(): boolean {
		if (audienceType === 'ORGANIZATION') return selectedOrg !== undefined;
		if (audienceType === 'USER') return selectedUser !== undefined;
		return true;
	}

	function isActionValid(): boolean {
		const hasUrl: boolean = actionUrl.trim().length > 0;
		const hasLabel: boolean = actionLabel.trim().length > 0;
		return hasUrl === hasLabel;
	}

	const canSubmit: boolean = $derived(
		title.trim().length > 0 &&
			message.trim().length > 0 &&
			isAudienceValid() &&
			isActionValid() &&
			!sending
	);

	const needsTypeConfirm: boolean = $derived(recipientCount !== null && recipientCount > 100);
	const confirmReady: boolean = $derived(
		!needsTypeConfirm || confirmInput === String(recipientCount)
	);

	function buildAudience(): BroadcastAudience {
		const audience: BroadcastAudience = { type: audienceType };
		if (audienceType === 'USER' && selectedUser) {
			audience.targetId = selectedUser.id;
		} else if (audienceType === 'ORGANIZATION' && selectedOrg) {
			audience.targetId = selectedOrg.id;
		} else if (audienceType === 'GLOBAL_ROLE') {
			audience.role = 'ADMIN';
		}
		return audience;
	}

	let previewDebounce: ReturnType<typeof setTimeout> | null = null;

	function schedulePreview(): void {
		if (previewDebounce) clearTimeout(previewDebounce);
		previewDebounce = setTimeout(async () => {
			if (!isAudienceValid()) {
				recipientCount = null;
				return;
			}
			previewLoading = true;
			try {
				const result = await previewRecipientCount(buildAudience());
				recipientCount = result.recipientCount;
			} catch {
				recipientCount = null;
			} finally {
				previewLoading = false;
			}
		}, 300);
	}

	$effect(() => {
		const _type = audienceType;
		const _user = selectedUser;
		const _org = selectedOrg;
		schedulePreview();
	});

	$effect(() => {
		if (orgDebounceTimer) clearTimeout(orgDebounceTimer);
		if (orgQuery.length >= 3) {
			orgDebounceTimer = setTimeout(async () => {
				orgSearching = true;
				showOrgDropdown = true;
				try {
					const result = await searchOrganizations({
						pageNumber: 0,
						pageSize: 10,
						searchInputs: [{ fieldName: 'search', textValue: orgQuery }]
					});
					orgResults = result.content ?? [];
				} catch {
					orgResults = [];
				} finally {
					orgSearching = false;
				}
			}, 300);
		} else {
			orgResults = [];
			showOrgDropdown = false;
		}
	});

	function selectOrg(org: OrganizationResponse): void {
		selectedOrg = org;
		orgQuery = '';
		orgResults = [];
		showOrgDropdown = false;
	}

	function clearOrg(): void {
		selectedOrg = undefined;
	}

	function openConfirm(): void {
		confirmInput = '';
		confirmOpen = true;
	}

	function resetForm(): void {
		category = 'ANNOUNCEMENT';
		title = '';
		message = '';
		audienceType = 'ALL_USERS';
		selectedUser = undefined;
		selectedOrg = undefined;
		actionUrl = '';
		actionLabel = '';
		recipientCount = null;
	}

	async function confirmSend(): Promise<void> {
		confirmOpen = false;
		sending = true;
		try {
			const result = await sendBroadcast({
				category,
				title: title.trim(),
				message: message.trim(),
				actionUrl: actionUrl.trim() || undefined,
				actionLabel: actionLabel.trim() || undefined,
				audience: buildAudience()
			});
			toast.success(`Sent to ${result.recipientCount} users`);
			resetForm();
		} catch (err: unknown) {
			const { message: errMsg } = handleError(err, 'Failed to send broadcast');
			toast.error(errMsg);
		} finally {
			sending = false;
		}
	}

	return {
		// Form state
		get category(): BroadcastCategory { return category; },
		set category(v: BroadcastCategory) { category = v; },
		get title(): string { return title; },
		set title(v: string) { title = v; },
		get message(): string { return message; },
		set message(v: string) { message = v; },
		get audienceType(): AudienceType { return audienceType; },
		set audienceType(v: AudienceType) {
			audienceType = v;
			selectedUser = undefined;
			selectedOrg = undefined;
		},
		get selectedUser(): UserResponse | undefined { return selectedUser; },
		set selectedUser(v: UserResponse | undefined) { selectedUser = v; },
		get selectedOrg(): OrganizationResponse | undefined { return selectedOrg; },
		get actionUrl(): string { return actionUrl; },
		set actionUrl(v: string) { actionUrl = v; },
		get actionLabel(): string { return actionLabel; },
		set actionLabel(v: string) { actionLabel = v; },
		// UI state
		get sending(): boolean { return sending; },
		get recipientCount(): number | null { return recipientCount; },
		get previewLoading(): boolean { return previewLoading; },
		get confirmOpen(): boolean { return confirmOpen; },
		set confirmOpen(v: boolean) { confirmOpen = v; },
		get confirmInput(): string { return confirmInput; },
		set confirmInput(v: string) { confirmInput = v; },
		// Org search
		get orgQuery(): string { return orgQuery; },
		set orgQuery(v: string) { orgQuery = v; },
		get orgResults(): OrganizationResponse[] { return orgResults; },
		get orgSearching(): boolean { return orgSearching; },
		get showOrgDropdown(): boolean { return showOrgDropdown; },
		// Derived
		get canSubmit(): boolean { return canSubmit; },
		get needsTypeConfirm(): boolean { return needsTypeConfirm; },
		get confirmReady(): boolean { return confirmReady; },
		// Methods
		isActionValid,
		selectOrg,
		clearOrg,
		openConfirm,
		confirmSend
	};
}

export type BroadcastSendService = ReturnType<typeof createBroadcastSendService>;
