package io.github.eventify.api.admin.model.projection;

import java.time.LocalDate;

public interface DailyGrowthData {

    LocalDate getDate();

    Long getTotal();

    Long getNew();

}
