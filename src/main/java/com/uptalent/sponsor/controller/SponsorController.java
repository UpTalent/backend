package com.uptalent.sponsor.controller;

import com.uptalent.payload.AuthResponse;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.service.SponsorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sponsors")
@RequiredArgsConstructor
@Tag(name = "Sponsor", description = "Sponsor APIs documentation")
public class SponsorController {
    private final SponsorService sponsorService;

    @Operation(
            summary = "Register new sponsor",
            description = "As a guest, I want to register on the site as sponsor",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = SponsorRegistration.class),
                            mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid fields"),
            @ApiResponse(responseCode = "201",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "User with email exists") })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerSponsor(@Valid @RequestBody SponsorRegistration sponsorRegistration) {
        return sponsorService.registerSponsor(sponsorRegistration);
    }
}
