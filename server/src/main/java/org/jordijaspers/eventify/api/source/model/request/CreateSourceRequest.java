package org.jordijaspers.eventify.api.source.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import static org.jordijaspers.eventify.Application.SERIAL_VERSION_UID;

/**
 * The CreateSourceRequest class represents the request payload for creating a source.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class CreateSourceRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    private String name;

    private String description;

}
