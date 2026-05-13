import { client } from '$lib/api/client';
import { isAuthenticated } from '$lib/stores/auth';
import { get } from 'svelte/store';
import type { NotificationResponse, PageResourceNotificationResponse } from '$lib/api/models';

const PAGE_SIZE: number = 20;
const POLL_INTERVAL_MS: number = 30_000;

class NotificationStore {
	#isPanelOpen: boolean = $state(false);
	#notifications: NotificationResponse[] = $state([]);
	#unreadCount: number = $state(0);
	#currentPage: number = $state(0);
	#totalPages: number = $state(0);
	#pollTimer: ReturnType<typeof setInterval> | null = null;
	#unsubscribeAuth: (() => void) | null = null;
	#onVisibilityChange: (() => void) | null = null;

	get isPanelOpen(): boolean {
		return this.#isPanelOpen;
	}

	get notifications(): NotificationResponse[] {
		return this.#notifications;
	}

	get unreadCount(): number {
		return this.#unreadCount;
	}

	get hasUnread(): boolean {
		return this.#unreadCount > 0;
	}

	get hasMore(): boolean {
		return this.#currentPage + 1 < this.#totalPages;
	}

	async #fetchNotifications(page: number = 0): Promise<void> {
		const { data } = await client.POST('/v1/notifications/search', {
			body: {
				pageNumber: page,
				pageSize: PAGE_SIZE,
				sortOrder: [{ name: 'createdAt', direction: 'DESC' }]
			}
		});
		const result: PageResourceNotificationResponse | undefined = data;
		if (!result) return;

		if (page === 0) {
			this.#notifications = result.content ?? [];
		} else {
			this.#notifications = [...this.#notifications, ...(result.content ?? [])];
		}
		this.#currentPage = result.pageNumber ?? 0;
		this.#totalPages = result.totalPages ?? 0;
	}

	async #fetchUnreadCount(): Promise<void> {
		if (!get(isAuthenticated)) return;
		const { data } = await client.GET('/v1/notifications/unread-count');
		if (data) {
			this.#unreadCount = data.count;
		}
	}

	#startPolling(): void {
		this.#stopPolling();
		this.#pollTimer = setInterval(async (): Promise<void> => {
			if (document.visibilityState === 'visible' && get(isAuthenticated)) {
				await this.#fetchUnreadCount();
			}
		}, POLL_INTERVAL_MS);
	}

	#stopPolling(): void {
		if (this.#pollTimer !== null) {
			clearInterval(this.#pollTimer);
			this.#pollTimer = null;
		}
	}

	init(): void {
		if (typeof document === 'undefined') return;
		// Guard against multiple init() calls
		if (this.#unsubscribeAuth !== null) return;

		this.#unsubscribeAuth = isAuthenticated.subscribe((authenticated: boolean): void => {
			if (authenticated) {
				this.#fetchUnreadCount();
				this.#startPolling();
			} else {
				this.#stopPolling();
				this.#notifications = [];
				this.#unreadCount = 0;
			}
		});

		this.#onVisibilityChange = (): void => {
			if (document.visibilityState === 'visible' && get(isAuthenticated)) {
				this.#fetchUnreadCount();
			}
		};
		document.addEventListener('visibilitychange', this.#onVisibilityChange);
	}

	destroy(): void {
		this.#stopPolling();
		this.#unsubscribeAuth?.();
		this.#unsubscribeAuth = null;
		if (this.#onVisibilityChange && typeof document !== 'undefined') {
			document.removeEventListener('visibilitychange', this.#onVisibilityChange);
			this.#onVisibilityChange = null;
		}
	}

	async openPanel(): Promise<void> {
		this.#isPanelOpen = true;
		await this.#fetchNotifications(0);
	}

	closePanel(): void {
		this.#isPanelOpen = false;
	}

	async markAsRead(id: number): Promise<void> {
		// Optimistic update
		const previous: NotificationResponse[] = this.#notifications;
		const previousCount: number = this.#unreadCount;
		this.#notifications = this.#notifications.map(
			(n: NotificationResponse): NotificationResponse =>
				n.id === id ? { ...n, readAt: new Date().toISOString() } : n
		);
		this.#unreadCount = Math.max(0, this.#unreadCount - 1);

		const { error } = await client.POST('/v1/notifications/{id}/read', {
			params: { path: { id } }
		});
		if (error) {
			this.#notifications = previous;
			this.#unreadCount = previousCount;
		}
	}

	async markAllAsRead(): Promise<void> {
		// Optimistic update
		const previous: NotificationResponse[] = this.#notifications;
		const previousCount: number = this.#unreadCount;
		const now: string = new Date().toISOString();
		this.#notifications = this.#notifications.map(
			(n: NotificationResponse): NotificationResponse => ({ ...n, readAt: now })
		);
		this.#unreadCount = 0;

		const { error } = await client.POST('/v1/notifications/read-all');
		if (error) {
			this.#notifications = previous;
			this.#unreadCount = previousCount;
		}
	}

	async loadMore(): Promise<void> {
		if (!this.hasMore) return;
		await this.#fetchNotifications(this.#currentPage + 1);
	}
}

export const notificationStore: NotificationStore = new NotificationStore();
