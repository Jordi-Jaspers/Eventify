package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class GroupRequest {

    private final String name;

    private final String description;

    private final Set<Long> checkIds = new HashSet<>();

}
