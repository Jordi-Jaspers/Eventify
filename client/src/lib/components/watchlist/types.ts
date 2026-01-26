import type { ChannelDetailsResponse } from '$lib/api/models';

// ================ Building Blocks (Source - Infinite/Copy Semantics) ===================

export type BlockItem = BlockChannelItem | BlockGroupItem;

export interface BlockChannelItem {
	id: string;
	type: 'block-channel';
}

export interface BlockGroupItem {
	id: string;
	type: 'block-group';
}

// ================ Config Items (Target - Actual Configuration) ===================

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

// ================ Union for Configurator Items ===================

export type ConfiguratorItem = ConfigItem | BlockItem;

// ================ Configuration JSON Structure ===================

export interface WatchlistConfiguration {
	items: Array<
		| { type: 'channel'; channelId: number }
		| { type: 'group'; name: string; channelIds: number[]; isExpanded: boolean }
	>;
}

// ================ DnD Types ===================

export type DndZoneType = 'blocks' | 'configurator' | `group-${string}`;

// ================ Utility Functions ===================

export function isBlockItem(item: ConfiguratorItem): item is BlockItem {
	return item.type === 'block-channel' || item.type === 'block-group';
}

export function isConfigItem(item: ConfiguratorItem): item is ConfigItem {
	return item.type === 'channel' || item.type === 'group';
}

export function isConfigChannelItem(item: ConfiguratorItem): item is ConfigChannelItem {
	return item.type === 'channel';
}

export function isConfigGroupItem(item: ConfiguratorItem): item is ConfigGroupItem {
	return item.type === 'group';
}
