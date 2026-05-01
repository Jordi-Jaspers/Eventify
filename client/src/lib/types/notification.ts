export type NotificationType = 'changelog' | 'system';

export interface NotificationItem {
	id: string;
	type: NotificationType;
	title: string;
	description?: string;
	date: string;
	read: boolean;
	actionLabel?: string;
	actionPath?: string;
}
