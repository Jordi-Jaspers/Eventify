import type {ChannelDetailsResponse} from '$lib/api/models';
import type {components} from '$lib/types/api';
import {SERVER_BASE_URL} from '$lib/config/constants';
import {toast} from 'svelte-sonner';

// Type alias for the event request from OpenAPI spec
type CreateEventRequest = components['schemas']['CreateEventRequest'];

/**
 * Slug validation pattern (lowercase letters, numbers, and dots only)
 */
export const SLUG_PATTERN: RegExp = /^[a-z0-9]+(\.[a-z0-9]+)*$/;

/**
 * Get badge variant for channel status
 */
export function getChannelStatusVariant(
    status: string | undefined
): 'success' | 'secondary' {
    return status === 'ACTIVE' ? 'success' : 'secondary';
}

/**
 * Get label for channel status
 */
export function getChannelStatusLabel(status: string | undefined): string {
    return status === 'ACTIVE' ? 'Active' : 'Paused';
}

/**
 * Validate channel slug format
 */
export function validateSlug(value: string): { valid: boolean; error: string } {
    if (!value.trim()) {
        return {valid: false, error: 'Slug is required'};
    }
    if (!SLUG_PATTERN.test(value)) {
        return {valid: false, error: 'Use only lowercase letters, numbers, and dots'};
    }
    return {valid: true, error: ''};
}

/**
 * Copy channel slug to clipboard with toast notification
 */
export function copySlugToClipboard(channel: ChannelDetailsResponse): void {
    if (!channel.slug) return;
    navigator.clipboard
        .writeText(channel.slug)
        .then(() => {
            toast.success('Slug copied to clipboard');
        })
        .catch(() => {
            toast.error('Failed to copy slug');
        });
}

/**
 * Generate curl command for posting events to a channel.
 * Uses OpenAPI spec types and configured API base URL.
 */
export function generateCurlCommand(channelId: number | undefined): string {
    const apiUrl = `${SERVER_BASE_URL}/v1/external/event`;
    const exampleBody: CreateEventRequest = {
        channelId: channelId,
        severity: 'OK',
        title: 'Event Title',
        message: 'Event message here',
        metadata: {}
    };

    const bodyJson = JSON.stringify(exampleBody, null, 2).replaceAll('\n', '\n  ');
    return String.raw`curl -X POST ${apiUrl} \
  -H "Authorization: Bearer <YOUR_API_KEY>" \
  -H "Content-Type: application/json" \
  -d '${bodyJson}'`;
}

/**
 * Copy curl command to clipboard with toast notification
 */
export function copyCurlToClipboard(channelId: number | undefined): void {
    const curlCommand: string = generateCurlCommand(channelId);
    navigator.clipboard
        .writeText(curlCommand)
        .then(() => {
            toast.success('Curl command copied to clipboard');
        })
        .catch(() => {
            toast.error('Failed to copy to clipboard');
        });
}
