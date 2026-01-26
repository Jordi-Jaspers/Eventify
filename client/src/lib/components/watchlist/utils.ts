import type { ConfigItem } from './types';

/**
 * Sort items in configurator: groups first, then standalone channels
 */
export function sortConfigItems(items: ConfigItem[]): ConfigItem[] {
	const groups = items.filter((item) => item.type === 'group');
	const channels = items.filter((item) => item.type === 'channel');
	return [...groups, ...channels];
}

/**
 * Generate unique ID for new items
 */
export function generateId(): string {
	return crypto.randomUUID();
}
