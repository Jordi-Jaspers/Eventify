package io.github.eventify.api.admin.model.projection;

import java.time.LocalDate;

/**
 * Projection interface for daily growth data.
 */
public interface DailyGrowthData {

    /**
     * Get the date.
     *
     * @return date
     */
    LocalDate getDate();

    /**
     * Get the total count.
     *
     * @return total count
     */
    Long getTotal();

    /**
     * Get the new count.
     *
     * @return new count
     */
    Long getNew();
}
