import type { ChannelDetailsResponse } from '$lib/api/models';

// ================ Config Items ===================

export type ConfigItem = ConfigChannelItem | ConfigGroupItem;

export interface ConfigChannelItem {
	id: string;
	type: 'channel';
	channelId: number;
	channel: ChannelDetailsResponse;
}

export interface ConfigGroupItem {
	id: string;
	type: 'group';
	name: string;
	channels: ConfigChannelItem[];
	isExpanded: boolean;
}

// ================ Type Guards ===================

export function isConfigChannelItem(item: ConfigItem): item is ConfigChannelItem {
	return item.type === 'channel';
}

export function isConfigGroupItem(item: ConfigItem): item is ConfigGroupItem {
	return item.type === 'group';
}
