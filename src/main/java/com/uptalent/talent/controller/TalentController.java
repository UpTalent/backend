package com.uptalent.talent.controller;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.payload.HttpResponse;
import com.uptalent.talent.model.response.TalentStatistic;
import com.uptalent.talent.service.TalentService;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
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


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/talents")
@Validated
@Tag(name = "Talent", description = "Talent APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class TalentController {
    private final TalentService talentService;

    @Operation(
            summary = "Retrieve list of talents",
            description = "As a guest, I want to be able to view talent information as a list on the page " +
                    "and see a limited information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = TalentGeneralInfo.class),
                    mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Illegal query params")})
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<TalentGeneralInfo> getAllTalents(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String [] skills){
        return talentService.getAllTalents(page, size, skills);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve talent profile",
            description = "As a talent, I want to be able to view the full information about a talent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = TalentProfile.class),
                    mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }) })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentProfile getTalentProfile(@PathVariable Long id){
        return talentService.getTalentProfileById(id);
    }

    @Operation(
            summary = "Register new talent",
            description = "As a guest, I want to register on the site as talent",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = TalentRegistration.class),
                    mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid fields"),
            @ApiResponse(responseCode = "201",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "User with email exists",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }) })
    @PostMapping
    public ResponseEntity<?> registerTalent(@Valid @RequestBody TalentRegistration talent){
        var response = talentService.addTalent(talent);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Talent edit profile",
            description = "As a talent, I would like to be able to edit my profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = TalentEdit.class),
                            mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = TalentOwnProfile.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot edit profile other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent with id does not exist",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }) })
    @PreAuthorize("hasAuthority('TALENT')")
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentOwnProfile updateTalent(@PathVariable Long id,
                                         @Valid @RequestBody TalentEdit updatedTalent){
        return talentService.updateTalent(id, updatedTalent);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Talent delete profile",
            description = "As a talent, I would like to be able to delete my profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot delete profile other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent with id does not exist",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }) })
    @PreAuthorize("hasAuthority('TALENT')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTalent(@PathVariable Long id) {
        talentService.deleteTalent(id);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Talent statistic",
            description = "As a talent, I would like to get statistic of my profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get statistic",
                    content = { @Content(schema = @Schema(implementation = TalentStatistic.class),
                    mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot delete profile other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PreAuthorize("hasAuthority('TALENT')")
    @GetMapping("/statistic")
    @ResponseStatus(HttpStatus.OK)
    public TalentStatistic getTalentStatistic() {
        return talentService.getStatistic();
    }
}
