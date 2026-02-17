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
export function generateCurlCommand(slug: string | undefined): string {
    const apiUrl = `${SERVER_BASE_URL}/v1/external/event`;
    const exampleBody: CreateEventRequest = {
        slug: slug,
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
 * Generate curl command for batch posting events to channels.
 * Uses OpenAPI spec types and configured API base URL.
 * Includes timestamp field to show historical import capability.
 */
export function generateBatchCurlCommand(): string {
    const apiUrl = `${SERVER_BASE_URL}/v1/external/event/batch`;
    const exampleEvents: CreateEventRequest[] = [
        {
            slug: 'your.channel.slug',
            severity: 'WARNING',
            title: 'Historical Event 1',
            message: 'First event from batch import',
            timestamp: '2024-01-15T10:30:00Z',
            metadata: {}
        },
        {
            slug: 'your.channel.slug',
            severity: 'OK',
            title: 'Historical Event 2',
            message: 'Second event from batch import',
            timestamp: '2024-01-15T11:00:00Z',
            metadata: {}
        }
    ];

    const batchBody = { events: exampleEvents };
    const bodyJson = JSON.stringify(batchBody, null, 2).replaceAll('\n', '\n  ');
    return String.raw`curl -X POST ${apiUrl} \
  -H "Authorization: Bearer <YOUR_API_KEY>" \
  -H "Content-Type: application/json" \
  -d '${bodyJson}'`;
}

/**
 * Copy curl command to clipboard with toast notification
 */
export function copyCurlToClipboard(slug: string | undefined): void {
    const curlCommand: string = generateCurlCommand(slug);
    navigator.clipboard
        .writeText(curlCommand)
        .then(() => {
            toast.success('Curl command copied to clipboard');
        })
        .catch(() => {
            toast.error('Failed to copy to clipboard');
        });
}

/**
 * Get relative activity time for last event
 * Returns "No activity" if lastEventAt is null
 */
export function getRelativeActivityTime(lastEventAt: string | null | undefined): string {
    if (!lastEventAt) return 'No activity';
    
    const date: Date = new Date(lastEventAt);
    const now: Date = new Date();
    const diffMs: number = now.getTime() - date.getTime();
    const diffMins: number = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min ago`;

    const diffHours: number = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;

    const diffDays: number = Math.floor(diffHours / 24);
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;

    const diffWeeks: number = Math.floor(diffDays / 7);
    if (diffWeeks < 4) return `${diffWeeks} week${diffWeeks > 1 ? 's' : ''} ago`;

    const diffMonths: number = Math.floor(diffDays / 30);
    return `${diffMonths} month${diffMonths > 1 ? 's' : ''} ago`;
}

/**
 * Get badge properties for stale status
 */
export function getStaleBadgeProps(isStale: boolean): {
    show: boolean;
    label: string;
    tooltip: string;
    className: string;
} {
    if (!isStale) {
        return {
            show: false,
            label: '',
            tooltip: '',
            className: ''
        };
    }
    
    return {
        show: true,
        label: 'Stale',
        tooltip: 'No events received in over 7 days',
        className: 'bg-amber-500/20 text-amber-600 dark:text-amber-400 border-amber-500/50'
    };
}
