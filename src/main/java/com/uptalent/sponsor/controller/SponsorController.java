package com.uptalent.sponsor.controller;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.payload.AuthResponse;
import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.kudos.model.response.KudosedProof;

import com.uptalent.proof.kudos.model.response.KudosedProofHistory;
import com.uptalent.sponsor.model.request.IncreaseKudos;
import com.uptalent.sponsor.model.request.SponsorEdit;
import com.uptalent.sponsor.model.request.SponsorLogin;
import com.uptalent.sponsor.model.request.SponsorRegistration;
import com.uptalent.sponsor.model.response.SponsorProfile;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sponsors")
@RequiredArgsConstructor
@Validated
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
                    content = { @Content(schema = @Schema(implementation = KudosedProof.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @GetMapping("/{sponsorId}/kudos")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<KudosedProof> getListKudosedProof(@PathVariable Long sponsorId,
                                                              @Min(value = 0, message = "Page should be greater or equals 0")
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @Positive(message = "Size should be positive")
                                                                  @RequestParam(defaultValue = "3") int size) {
        return sponsorService.getListKudosedProofBySponsorId(sponsorId, page, size);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get list of transactions of kudosed proof",
            description = "The ability to see the number of Kudos I have given to the proof")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = KudosedProofHistory.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @GetMapping("/{sponsorId}/kudos/{proofId}/history")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<KudosedProofHistory> getListKudosedProofHistory(@PathVariable Long sponsorId,
                                                                            @PathVariable Long proofId,
                                                                            @Min(value = 0, message = "Page should be greater or equals 0") @RequestParam(defaultValue = "0") int page,
                                                                            @Positive(message = "Size should be positive")
                                                                            @RequestParam(defaultValue = "10") int size) {
        return sponsorService.getListKudosedProofHistoryBySponsorIdAndProofId(sponsorId, proofId, page, size);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get sponsor profile",
            description = "The ability to get sponsor profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = SponsorProfile.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Sponsor was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @GetMapping("/{sponsorId}")
    @ResponseStatus(HttpStatus.OK)
    public SponsorProfile getSponsorProfile(@PathVariable Long sponsorId) {
        return sponsorService.getSponsorProfileById(sponsorId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Edit sponsor profile",
            description = "The ability to edit sponsor profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = SponsorEdit.class),
                    mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = SponsorProfile.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Sponsor was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @PatchMapping("/{sponsorId}")
    @ResponseStatus(HttpStatus.OK)
    public SponsorProfile editSponsorProfile(@PathVariable Long sponsorId,
                                             @Valid @RequestBody SponsorEdit updatedSponsor) {
        return sponsorService.editSponsor(sponsorId, updatedSponsor);
    }

    @Operation(
            summary = "Sponsor log in",
            description = "As a guest, I want to log in on the site as sponsor",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = SponsorLogin.class),
                            mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Sponsor with email was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> login(@Valid @RequestBody SponsorLogin loginRequest) {
        var response = sponsorService.login(loginRequest);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add kudos to sponsor profile",
            description = "The ability to increase quantity of kudos")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = KudosedProofHistory.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Illegal field",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('SPONSOR')")
    @PutMapping("/{sponsorId}/kudos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addKudos(@PathVariable Long sponsorId,
                         @Valid @RequestBody IncreaseKudos increaseKudos) {
        sponsorService.addKudos(sponsorId, increaseKudos);
    }

}
