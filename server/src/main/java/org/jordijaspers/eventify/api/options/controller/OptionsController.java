package org.jordijaspers.eventify.api.options.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.jordijaspers.eventify.api.Paths.OPTIONS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class OptionsController {

    @ResponseStatus(OK)
    @Operation(summary = "Retrieve the list of all selectable constants for an options menu (Authorities, ...).")
    @GetMapping(
        path = OPTIONS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> retrieveOptions() {
        return ResponseEntity.status(OK).body(
            Map.of(
                "authorities",
                Authority.getAll()
            )
        );
    }

}
