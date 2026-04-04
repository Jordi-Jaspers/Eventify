<script lang="ts">
	import { formatTime, formatDate } from '$lib/utils/date';
	import { formatDurationLength } from '$lib/utils/duration';
	import type { TimelineDuration, Severity } from '$lib/api/models';
	import { getSeverityColors } from './types';
	import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '$lib/components/ui/tooltip';
    import { ChevronLeft, ChevronRight, MoreVertical, ArrowRight } from '@lucide/svelte';

	interface Props {
		durations: TimelineDuration[];
		selectedDuration: TimelineDuration | null;
        canGoPrevious: boolean;
        canGoNext: boolean;
        hasPrevious: boolean;  // For visual indicators (cutoff markers)
		onDurationClick: (duration: TimelineDuration) => void;
        onPrevious: () => void;
        onNext: () => void;
	}

	let props: Props = $props();
	
	// Derived values to maintain reactivity from props
	const durations = $derived(props.durations);
	const selectedDuration = $derived(props.selectedDuration);
	const canGoPrevious = $derived(props.canGoPrevious);
	const canGoNext = $derived(props.canGoNext);
	const hasPrevious = $derived(props.hasPrevious);
	const onDurationClick = $derived(props.onDurationClick);
	const onPrevious = $derived(props.onPrevious);
	const onNext = $derived(props.onNext);
	
	// Debug logging
	$effect(() => {
		console.log('[MiniTimeline] props changed:', {
			canGoPrevious: props.canGoPrevious,
			canGoNext: props.canGoNext,
			derivedCanGoPrevious: canGoPrevious,
			derivedCanGoNext: canGoNext
		});
	});

    const COLLAPSE_THRESHOLD_MS = 24 * 60 * 60 * 1000; // 24 hours

    // Process durations for display (calculate widths, visual weights)
    const segments = $derived.by(() => {
        if (!durations || durations.length === 0) return [];

        const now = Date.now();
        const processed = durations.map(d => {
            const start = new Date(d.startTime).getTime();
            const end = d.endTime ? new Date(d.endTime).getTime() : now;
            const realDuration = end - start;
            const isOngoing = !d.endTime;
            const isCollapsed = realDuration >= COLLAPSE_THRESHOLD_MS;
            
            // Visual weight logic
            const visualWeight = isCollapsed ? (COLLAPSE_THRESHOLD_MS / 4) : realDuration;

            return {
                duration: d,
                realDuration,
                visualWeight,
                isOngoing,
                isCollapsed,
                start,
                end
            };
        });

        const totalVisualWeight = processed.reduce((sum, p) => sum + p.visualWeight, 0);

        return processed.map(p => {
            // Calculate percentage width
            // Ensure a minimum width logic is handled by CSS min-width if needed
            let widthPercent = totalVisualWeight > 0 ? (p.visualWeight / totalVisualWeight) * 100 : 0;
            
            return {
                ...p,
                widthPercent
            };
        });
    });

	function getSegmentClasses(severity: Severity): string {
		switch (severity) {
			case 'CRITICAL':
				return 'bg-red-500 shadow-red-500/30';
			case 'WARNING':
				return 'bg-amber-500 shadow-amber-500/30';
			case 'OK':
				return 'bg-green-500 shadow-green-500/30';
			case 'NO_DATA':
				return 'bg-gray-400 shadow-gray-400/20';
		}
	}
    
    // Formatting helpers
    function formatCollapsedLabel(ms: number): string {
        const days = Math.floor(ms / (24 * 60 * 60 * 1000));
        return `${days}d`;
    }
</script>

<div class="space-y-1.5 select-none">
	<!-- Navigation and Stats -->
	<div class="flex items-center justify-between text-xs text-muted-foreground">
		<button
			type="button"
			class="flex items-center gap-1 px-2 py-0.5 rounded hover:bg-muted/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors text-primary font-medium"
			disabled={!canGoPrevious}
			onclick={onPrevious}
		>
            <ChevronLeft class="h-3 w-3" />
			Previous
		</button>
		
        {#if selectedDuration}
            <span class="font-mono text-[10px]">
                {formatDate(selectedDuration.startTime)}
            </span>
        {/if}
		
		<button
			type="button"
			class="flex items-center gap-1 px-2 py-0.5 rounded hover:bg-muted/50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors text-primary font-medium"
			disabled={!canGoNext}
			onclick={onNext}
		>
			Next
            <ChevronRight class="h-3 w-3" />
		</button>
	</div>

	<!-- Timeline Track (Flexbox for non-linear time) -->
	<div class="relative h-10 bg-muted/10 rounded-md border border-border/20 overflow-hidden flex w-full">
		{#each segments as item, i (item.duration.startTime)}
            {@const isSelected = selectedDuration && 
                item.duration.startTime === selectedDuration.startTime && 
                item.duration.endTime === selectedDuration.endTime}
            {@const colors = getSeverityColors(item.duration.severity)}
            
            <!-- Segment -->
            <TooltipProvider>
                <Tooltip>
                    <TooltipTrigger 
                        class="relative h-full transition-all duration-200 ease-out group focus:outline-none flex-shrink-0"
                        style="width: {item.widthPercent}%; min-width: 4px;"
                        onclick={() => onDurationClick(item.duration)}
                    >
                        <div class="h-full w-full py-1 px-[1px]">
                            <div 
                                class="h-full rounded-sm shadow-sm transition-all duration-200 flex items-center justify-center relative overflow-hidden
                                    {getSegmentClasses(item.duration.severity)}
                                    {isSelected 
                                        ? 'ring-2 ring-primary ring-offset-1 ring-offset-card shadow-lg z-10' 
                                        : 'opacity-60 hover:opacity-90 group-hover:shadow-md'
                                    }"
                            >
                                <!-- Collapsed Indicator -->
                                {#if item.isCollapsed}
                                    <div class="flex items-center justify-center gap-0.5 text-white/90 text-[10px] font-bold z-10 relative">
                                        {#if i === 0 && hasPrevious}
                                            <MoreVertical class="h-3 w-3 -ml-1 opacity-70" />
                                        {/if}
                                        {formatCollapsedLabel(item.realDuration)}
                                    </div>
                                    <!-- Hatched pattern overlay for collapsed -->
                                    <div class="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI0IiBoZWlnaHQ9IjQiPgo8cmVjdCB3aWR0aD0iNCIgaGVpZ2h0PSI0IiBmaWxsPSIjZmZmIiBmaWxsLW9wYWNpdHk9IjAuMSIvPgo8cGF0aCBkPSJNLTEgMUw1IC0xTS0xIDVMNSAzIiBzdHJva2U9IiMwMDAiIHN0cm9rZS1vcGFjaXR5PSIwLjEiIHN0cm9rZS13aWR0aD0iMSIvPjwvc3ZnPg==')] opacity-30"></div>
                                {/if}

                                <!-- Ongoing Indicator -->
                                {#if item.isOngoing}
                                    <div class="absolute right-0 top-0 bottom-0 w-1 bg-white/50 animate-pulse"></div>
                                    <ArrowRight class="h-3 w-3 text-white absolute right-1 top-1/2 -translate-y-1/2 opacity-80" />
                                {/if}

                                <!-- Start Cutoff Indicator (for first item if previous exists) -->
                                {#if i === 0 && hasPrevious && !item.isCollapsed}
                                    <MoreVertical class="absolute left-1 h-3 w-3 text-white/70" />
                                {/if}
                            </div>
                        </div>
                    </TooltipTrigger>
                    <TooltipContent side="bottom" class="text-xs bg-popover text-popover-foreground border border-border shadow-lg">
                        <div class="space-y-1">
                            <div class="flex items-center gap-2">
                                <span class="font-semibold {colors.text}">{item.duration.severity}</span>
                                {#if item.isCollapsed}
                                    <span class="text-[10px] bg-muted px-1 rounded">Collapsed (>24h)</span>
                                {/if}
                                {#if item.isOngoing}
                                    <span class="text-[10px] bg-primary/20 text-primary px-1 rounded animate-pulse">Ongoing</span>
                                {/if}
                            </div>
                            <p class="text-muted-foreground font-mono">
                                {formatTime(item.duration.startTime)} – {item.duration.endTime ? formatTime(item.duration.endTime) : 'Now'}
                            </p>
                            <p class="text-muted-foreground/80">
                                Duration: {formatDurationLength(item.duration.startTime, item.duration.endTime ?? new Date())}
                            </p>
                            {#if i === 0 && hasPrevious}
                                <p class="text-[10px] text-muted-foreground mt-1 border-t border-border/50 pt-1">
                                    Started: {formatDate(item.duration.startTime)}
                                </p>
                            {/if}
                        </div>
                    </TooltipContent>
                </Tooltip>
            </TooltipProvider>
        {/each}
	</div>
    
    <!-- Legend / Hints -->
    <div class="flex justify-between px-1">
         <!-- Start Time of Window -->
         <span class="text-[10px] text-muted-foreground/50 font-mono">
            {#if segments.length > 0}
                {formatTime(segments[0].duration.startTime)}
            {/if}
         </span>
         
         <!-- End Time of Window -->
         <span class="text-[10px] text-muted-foreground/50 font-mono">
            {#if segments.length > 0}
                {@const last = segments[segments.length-1]}
                {last.duration.endTime ? formatTime(last.duration.endTime) : 'Now'}
            {/if}
         </span>
    </div>
</div>
