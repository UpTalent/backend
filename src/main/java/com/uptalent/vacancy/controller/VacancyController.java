package com.uptalent.vacancy.controller;

import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.model.enums.ContentStatus;
import com.uptalent.util.annotation.EnumValue;
import com.uptalent.vacancy.model.response.VacancyGeneralInfo;
import com.uptalent.vacancy.service.VacancyService;
import com.uptalent.vacancy.model.response.VacancyDetailInfo;
import com.uptalent.vacancy.model.request.VacancyModify;
import com.uptalent.vacancy.submission.model.request.SubmissionRequest;
import com.uptalent.vacancy.submission.model.response.SubmissionResponse;
import com.uptalent.vacancy.submission.model.response.TalentSubmission;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
                    content = { @Content(schema = @Schema(implementation = VacancyDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Do not have permission",
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

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve list of vacancies from sponsor profile",
            description = "As a sponsor, I want my vacancies to be displayed on my profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = VacancyDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Illegal query params",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot list of vacancies " +
                    "which has not PUBLISHED status from other sponsor profile",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Sponsor by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @GetMapping("/sponsors/{sponsor-id}")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<VacancyDetailInfo> getAllSponsorVacancies(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "published")
            @EnumValue(enumClass = ContentStatus.class) String status,
            @RequestParam(defaultValue = "desc") String sort,
            @PathVariable("sponsor-id") Long sponsorId) {
        return vacancyService.getSponsorVacancies(page, size, sort, sponsorId, status);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete vacancy",
            description = "As a sponsor, I want to delete my vacancy.")
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "Unrelated vacancy",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })
    })
    @DeleteMapping("/{vacancyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SPONSOR')")
    public ResponseEntity<?> deleteVacancy(@PathVariable Long vacancyId) {
        vacancyService.deleteVacancy(vacancyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get all vacancies",
            description = "As a user, I want to get a list of vacancies.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = VacancyGeneralInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<VacancyGeneralInfo> getAllVacancies(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(required = false) String [] skills) {
        return vacancyService.getVacancies(page, size, sort, skills);
    }


    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Apply submission",
            description = "As a talent, I want to apply submission for the vacancy.")
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    content = { @Content(schema = @Schema(implementation = SubmissionResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page/No matched skills",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Duplicate submission",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })
    })
    @PostMapping("/{vacancyId}/submissions")
    @PreAuthorize("hasAuthority('TALENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse createSubmission(@PathVariable Long vacancyId,
                                               @Valid @RequestBody SubmissionRequest submissionRequest){
        return vacancyService.createSubmission(vacancyId, submissionRequest);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get talent's submissions",
            description = "As a talent, I want to view my submissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = TalentSubmission.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid query params",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })
    })
    @GetMapping("/submissions/my")
    @PreAuthorize("hasAuthority('TALENT')")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<TalentSubmission> getMySubmissions(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "9") int size){
        return vacancyService.getTalentSubmissions(page, size);
    }
}
