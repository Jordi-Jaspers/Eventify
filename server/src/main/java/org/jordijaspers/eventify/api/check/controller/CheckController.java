package org.jordijaspers.eventify.api.check.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.check.model.mapper.CheckMapper;
import org.jordijaspers.eventify.api.check.model.response.CheckResponse;
import org.jordijaspers.eventify.api.check.service.CheckService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.jordijaspers.eventify.api.Paths.CHECK_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the checks.
 */
@RestController
@RequiredArgsConstructor
public class CheckController {

    private final CheckService checkService;

    private final CheckMapper checkMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Fuzzy-search through checks by name with default page size 25")
    @GetMapping(
        path = CHECK_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<CheckResponse>> searchChecks(@RequestParam("q") final String query,
        @RequestParam(defaultValue = "0") final int page,
        @RequestParam(defaultValue = "25") final int size) {
        final Page<Check> checkPage = checkService.fuzzySearchChecks(query, page, size);
        final Page<CheckResponse> response = checkPage.map(checkMapper::toCheckResponse);
        return ResponseEntity.status(OK).body(response);
    }
}
