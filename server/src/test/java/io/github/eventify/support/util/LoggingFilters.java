package io.github.eventify.support.util;

import io.github.jframe.logging.filter.MockMvcFilter;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Holds all filters used while testing.
 */
@Component
public class LoggingFilters {

    /**
     * Spring security filter chain.
     */
    @Autowired
    protected Filter springSecurityFilterChain;

    /**
     * Kibana log cleanup filter.
     */
    @Autowired
    private List<MockMvcFilter> loggingFilters;

    /**
     * @return all filters used while testing.
     */
    public Filter[] getFilters() {
        final List<Filter> filters = new ArrayList<>(loggingFilters);
        filters.add(springSecurityFilterChain);
        return filters.toArray(new Filter[] {});
    }
}
