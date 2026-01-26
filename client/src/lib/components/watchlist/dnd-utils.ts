import type { ConfiguratorItem } from './types';

/**
 * Sort items in configurator: groups first, then standalone channels
 */
export function sortConfiguratorItems<T extends ConfiguratorItem>(items: T[]): T[] {
	const groups: T[] = items.filter((i: T) => i.type === 'group');
	const channels: T[] = items.filter((i: T) => i.type === 'channel');
	return [...groups, ...channels];
}

/**
 * Generate unique ID for new items
 */
export function generateId(): string {
	return crypto.randomUUID();
}
