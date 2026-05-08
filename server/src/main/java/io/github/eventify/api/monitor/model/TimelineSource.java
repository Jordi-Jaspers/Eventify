package io.github.eventify.api.monitor.model;

/**
 * Interface for any entity that can provide a timeline.
 *
 * <p>This abstraction enables generic timeline consolidation across different grouping structures:
 * <ul>
 * <li>Channel - provides its own timeline from events</li>
 * <li>ChannelGroup (future) - provides a consolidated timeline from its members</li>
 * <li>Dashboard (conceptually) - consolidation of all top-level timeline sources</li>
 * </ul>
 *
 * <p>Example hierarchy:
 * <pre>
 * Dashboard (consolidated from all sources)
 * ├── Group A (consolidated from Channel A + B)
 * │ ├── Channel A (timeline)
 * │ └── Channel B (timeline)
 * └── Channel C (timeline)
 * </pre>
 *
 * <p>The same consolidation algorithm works at any level because everything is a TimelineSource.
 */
@FunctionalInterface
public interface TimelineSource {

    /**
     * Returns the timeline for this source.
     *
     * @return the timeline, never null (use {@link Timeline#empty()} for empty timelines)
     */
    Timeline getTimeline();
}
