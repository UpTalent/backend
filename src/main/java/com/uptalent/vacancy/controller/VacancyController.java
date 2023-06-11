package com.uptalent.vacancy.controller;

import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.vacancy.service.VacancyService;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.headers.Header;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacancies")
@Validated
@Tag(name = "Vacancy", description = "Vacancy APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class VacancyController {
    private final VacancyService vacancyService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create vacancy",
            description = "As a sponsor, I want to be able to create vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "201", headers = {@Header(name = "location", description = "vacancy-id")}),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You do not have a permission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Sponsor by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PostMapping
    @PreAuthorize("hasAuthority('SPONSOR')")
    public ResponseEntity<?> createVacancy(@Valid @RequestBody VacancyModify vacancyModify) {
        URI vacancyLocation = vacancyService.createVacancy(vacancyModify);

        return ResponseEntity.created(vacancyLocation).build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve vacancy detail",
            description = "As a user, I want to get vacancy detail.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = ProofDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Vacancy by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public VacancyDetailInfo getVacancyDetailInfo(@PathVariable Long id) {
        return vacancyService.getVacancy(id);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Edit/Publish/Hide/Reopen vacancy",
            description = "As a sponsor, I want to be able to Edit/Publish/Hide/Reopen vacancy")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = VacancyDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot update vacancy for other sponsor",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent or vacancy by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Illegal operation",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('SPONSOR')")
    public VacancyDetailInfo updateVacancy(@PathVariable Long id, @Valid @RequestBody VacancyModify vacancyModify) {
        return vacancyService.updateVacancy(id, vacancyModify);
    }
}