package com.uptalent.sponsor.controller;

import com.uptalent.payload.AuthResponse;
import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.kudos.model.response.KudosedProofDetail;
import com.uptalent.proof.kudos.model.response.KudosedProof;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.service.SponsorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sponsors")
@RequiredArgsConstructor
@Tag(name = "Sponsor", description = "Sponsor APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get list of kudosed proofs",
            description = "The ability to see the number of Kudos I have given to proofs")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = KudosedProofDetail.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @GetMapping("/{sponsorId}/kudos")
    @ResponseStatus(HttpStatus.OK)
    public List<KudosedProofDetail> getListKudosedProof(@PathVariable Long sponsorId) {
        return sponsorService.getListKudosedProofDetailsBySponsorId(sponsorId);
    }
}
