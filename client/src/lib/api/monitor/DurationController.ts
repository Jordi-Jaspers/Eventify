import { client } from '$lib/api/client';
import type { components } from '$lib/types/api';

type DurationDetailsResponse = components['schemas']['DurationDetailsResponse'];
type Direction = 'AROUND' | 'BEFORE' | 'AFTER';

export async function fetchUserChannelDurations(
    channelId: number,
    timestamp: string,
    direction: Direction
): Promise<DurationDetailsResponse> {
    const { data, error } = await client.POST('/v1/user/channel/{id}/durations', {
        params: { path: { id: channelId } },
        body: { timestamp, direction }
    });
    if (error) throw error;
    return data;
}

export async function fetchOrgChannelDurations(
    orgId: number,
    channelId: number,
    timestamp: string,
    direction: Direction
): Promise<DurationDetailsResponse> {
    const { data, error } = await client.POST('/v1/organization/{orgId}/channels/{id}/durations', {
        params: { path: { orgId, id: channelId } },
        body: { timestamp, direction }
    });
    if (error) throw error;
    return data;
}
