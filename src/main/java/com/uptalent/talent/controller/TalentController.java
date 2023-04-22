package com.uptalent.talent.controller;

import com.uptalent.filestore.FileStoreOperation;
import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.payload.AuthResponse;
import com.uptalent.talent.service.TalentService;
import com.uptalent.talent.model.request.TalentEdit;
import com.uptalent.talent.model.request.TalentLogin;
import com.uptalent.talent.model.request.TalentRegistration;
import com.uptalent.talent.model.response.TalentGeneralInfo;
import com.uptalent.talent.model.response.TalentOwnProfile;
import com.uptalent.talent.model.response.TalentProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


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
            @RequestParam(defaultValue = "9") int size){
        return talentService.getAllTalents(page, size);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve talent profile",
            description = "As a talent, I want to be able to view the full information about a talent.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = TalentProfile.class),
                    mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields"),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page"),
            @ApiResponse(responseCode = "404", description = "Talent not found") })
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
            @ApiResponse(responseCode = "409", description = "User with email exists") })
    @PostMapping
    public ResponseEntity<?> registerTalent(@Valid @RequestBody TalentRegistration talent){
        var response = talentService.addTalent(talent);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Talent log in",
            description = "As a guest, I want to log in on the site as talent",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = TalentLogin.class),
                            mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "404", description = "Talent with email does not exist") })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody TalentLogin loginRequest){
        var response = talentService.login(loginRequest);

        return new ResponseEntity<>(response, HttpStatus.OK);
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
            @ApiResponse(responseCode = "400", description = "Invalid fields"),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page"),
            @ApiResponse(responseCode = "403", description = "You cannot edit profile other talent"),
            @ApiResponse(responseCode = "404", description = "Talent with id does not exist") })
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
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page"),
            @ApiResponse(responseCode = "403", description = "You cannot delete profile other talent"),
            @ApiResponse(responseCode = "404", description = "Talent with id does not exist") })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTalent(@PathVariable Long id) {
        talentService.deleteTalent(id);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Image upload to talent's profile",
            description = "As a talent, I would like to be able to change the photo or banner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request") })
    @PostMapping(
            path = "/{id}/image/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void uploadImage(@PathVariable Long id,
                            @Parameter(required = true, description = "Image file") @RequestParam MultipartFile image,
                            @Parameter(required = true, description = "Operation for uploading AVATAR or BANNER")
                                @RequestParam FileStoreOperation operation){
        talentService.uploadImage(id, image, operation);
    }


}
