import { fetchUserChannelDurations, fetchOrgChannelDurations } from '../DurationController';
import type { TimelineDuration } from '$lib/api/models';
import { handleError } from '$lib/utils/error-handler';
import { toast } from 'svelte-sonner';

export function createDurationService() {
    let durations: TimelineDuration[] = $state([]);
    let selectedIndex: number = $state(-1);
    let hasPrevious: boolean = $state(false);
    let hasNext: boolean = $state(false);
    let loading: boolean = $state(false);
    let error: string | null = $state(null);

    let currentChannelId: number | null = null;
    let currentOrgId: number | undefined = undefined;
    let fetchRequestId: number = 0;

    const selectedDuration = $derived(
        selectedIndex >= 0 && selectedIndex < durations.length 
            ? durations[selectedIndex] 
            : null
    );
    
    // Can navigate to previous: either move within window OR fetch older window
    const canGoPrevious = $derived(selectedIndex > 0 || hasPrevious);
    
    // Can navigate to next: either move within window OR fetch newer window
    const canGoNext = $derived(selectedIndex < durations.length - 1 || hasNext);

    async function fetch(timestamp: string, direction: 'AROUND' | 'BEFORE' | 'AFTER'): Promise<void> {
        if (!currentChannelId) return;
        
        const requestId: number = ++fetchRequestId;
        loading = true;
        error = null;
        
        try {
            const fetchFn = currentOrgId 
                ? () => fetchOrgChannelDurations(currentOrgId!, currentChannelId!, timestamp, direction)
                : () => fetchUserChannelDurations(currentChannelId!, timestamp, direction);
                
            const response = await fetchFn();

            // Discard stale response if a newer request has been fired
            if (requestId !== fetchRequestId) return;
            
            durations = response.durations;
            selectedIndex = response.selectedIndex;
            hasPrevious = response.hasPrevious;
            hasNext = response.hasNext;
            
        } catch (err: unknown) {
            if (requestId !== fetchRequestId) return;
            const { message } = handleError(err, 'Failed to load durations');
            error = message;
            toast.error(message);
        } finally {
            if (requestId === fetchRequestId) {
                loading = false;
            }
        }
    }

    async function load(channelId: number, orgId: number | undefined, timestamp: string): Promise<void> {
        currentChannelId = channelId;
        currentOrgId = orgId;
        await fetch(timestamp, 'AROUND');
    }

    async function goToPrevious(): Promise<void> {
        if (loading) return;
        
        if (selectedIndex > 0) {
            selectedIndex--;
        } else if (hasPrevious && durations.length > 0) {
            await fetch(durations[0].startTime, 'BEFORE');
        }
    }

    async function goToNext(): Promise<void> {
        if (loading) return;
        
        if (selectedIndex < durations.length - 1) {
            selectedIndex++;
        } else if (hasNext && durations.length > 0) {
            const lastEndTime = durations[durations.length - 1].endTime ?? new Date().toISOString();
            await fetch(lastEndTime, 'AFTER');
        }
    }
    
    function selectDuration(duration: TimelineDuration): void {
        const index = durations.findIndex(d => d.startTime === duration.startTime && d.endTime === duration.endTime);
        if (index !== -1) {
            selectedIndex = index;
        }
    }

    return {
        get durations() { return durations; },
        get selectedIndex() { return selectedIndex; },
        get selectedDuration() { return selectedDuration; },
        get hasPrevious() { return hasPrevious; },
        get hasNext() { return hasNext; },
        get canGoPrevious() { return canGoPrevious; },
        get canGoNext() { return canGoNext; },
        get loading() { return loading; },
        get error() { return error; },
        load,
        goToPrevious,
        goToNext,
        selectDuration
    };
}

export type DurationService = ReturnType<typeof createDurationService>;
