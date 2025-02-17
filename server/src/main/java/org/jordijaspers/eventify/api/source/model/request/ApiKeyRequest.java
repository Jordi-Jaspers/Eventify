package org.jordijaspers.eventify.api.source.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * The CreateSourceRequest class represents the request payload for creating a source.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class ApiKeyRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private ZonedDateTime expiresAt = ZonedDateTime.now().plusYears(100);

}
