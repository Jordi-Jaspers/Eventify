package io.github.eventify.api.notification.adapter.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO for adapter connection test results.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestConnectionResponse {

    private boolean success;
    private String error;

    /**
     * Creates a successful test connection response.
     *
     * @return a response with success=true and no error
     */
    public static TestConnectionResponse passed() {
        return new TestConnectionResponse(true, null);
    }

    /**
     * Creates a failed test connection response.
     *
     * @param error the error message describing the failure
     * @return a response with success=false and the provided error
     */
    public static TestConnectionResponse failed(final String error) {
        return new TestConnectionResponse(false, error);
    }
}
