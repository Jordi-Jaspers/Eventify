import { versionStore } from '$lib/stores/version.svelte';
import { getLatestVersion, changelog } from '$lib/data/changelog';
import type { NotificationItem } from '$lib/types/notification';
import type { ChangelogEntry } from '$lib/types/changelog';

const MAX_NOTIFICATIONS: number = 5;

function compareVersions(a: string, b: string): number {
	const partsA: number[] = a.split('.').map(Number);
	const partsB: number[] = b.split('.').map(Number);
	for (let i: number = 0; i < Math.max(partsA.length, partsB.length); i++) {
		const diff: number = (partsA[i] ?? 0) - (partsB[i] ?? 0);
		if (diff !== 0) return diff;
	}
	return 0;
}

class NotificationStore {
	#isPanelOpen: boolean = $state(false);

	get isPanelOpen(): boolean {
		return this.#isPanelOpen;
	}

	get notifications(): NotificationItem[] {
		const lastSeen: string = versionStore.lastSeenVersion;

		return changelog
			.filter((entry: ChangelogEntry): boolean => !lastSeen || compareVersions(entry.version, lastSeen) > 0)
			.slice(0, MAX_NOTIFICATIONS)
			.map((entry: ChangelogEntry): NotificationItem => ({
				id: `changelog-${entry.version}`,
				type: 'changelog',
				title: `Version ${entry.version} released`,
				description: entry.features?.[0] ?? entry.improvements?.[0] ?? undefined,
				date: entry.date,
				read: false,
				actionLabel: 'View full changelog',
				actionPath: '/changelog'
			}));
	}

	get unreadCount(): number {
		return this.notifications.length;
	}

	get hasUnread(): boolean {
		return this.unreadCount > 0;
	}

	markAllAsRead(): void {
		versionStore.markAsSeen();
	}

	openPanel(): void {
		this.#isPanelOpen = true;
	}

	closePanel(): void {
		this.#isPanelOpen = false;
	}
}

export const notificationStore: NotificationStore = new NotificationStore();
