package org.jordijaspers.eventify.api.source.model.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DetailedSourceResponse extends SourceResponse {

    private ApiKeyResponse apiKey;

}
