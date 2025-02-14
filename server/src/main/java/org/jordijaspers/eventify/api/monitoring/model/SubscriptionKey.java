package org.jordijaspers.eventify.api.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@Data
@AllArgsConstructor
public class SubscriptionKey {

    private Long dashboardId;

    private Duration window;

}
