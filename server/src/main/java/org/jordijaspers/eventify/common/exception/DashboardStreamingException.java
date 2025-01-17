package org.jordijaspers.eventify.common.exception;

import java.io.Serial;

import org.hawaiiframework.exception.ApiException;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.DASHBOARD_STREAMING_ERROR;

/**
 * Exception thrown when streaming from the dashboard fails.
 */
public class DashboardStreamingException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Default constructor.
     */
    public DashboardStreamingException() {
        super(DASHBOARD_STREAMING_ERROR);
    }
}
