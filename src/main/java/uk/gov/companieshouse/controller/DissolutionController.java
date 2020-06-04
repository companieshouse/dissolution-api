package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dissolution")
public class DissolutionController {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DissolutionController.class);

    @Operation(summary = "Check if Dissolution API is working", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution API is up"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthcheck() {

        LOGGER.debug("Healthcheck endpoint hit.");

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
