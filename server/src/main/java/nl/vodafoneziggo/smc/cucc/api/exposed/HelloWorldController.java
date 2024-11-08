package nl.vodafoneziggo.smc.cucc.api.exposed;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
public class HelloWorldController {

    @ResponseStatus(OK)
    @Operation(summary = "Hello.")
    @GetMapping(path = "/hello")
    public ResponseEntity<String> getHelloMessage() {
        return ResponseEntity.ok("Hello, World!");
    }
}
