package org.jordijaspers.smc.eventify.test_support;

import org.hawaiiframework.logging.model.MockMvcFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.Filter;

/**
 * Holds all filters used while testing.
 */
@Component
@Profile("itest")
public class HawaiiFilters {

    /**
     * Spring security filter chain.
     */
    @Autowired
    protected Filter springSecurityFilterChain;

    /**
     * Kibana log cleanup filter.
     */
    @Autowired
    private List<MockMvcFilter> defaultHawaiiFilters;

    /**
     * @return all filters used while testing.
     */
    public Filter[] getFilters() {
        final List<Filter> filters = new ArrayList<>(defaultHawaiiFilters);
        filters.add(springSecurityFilterChain);
        return filters.toArray(new Filter[] {});
    }
}
