package com.uptalent.proof.controller;


import com.uptalent.pagination.PageWithMetadata;
import com.uptalent.payload.HttpResponse;
import com.uptalent.proof.kudos.model.request.PostKudos;
import com.uptalent.proof.kudos.model.response.KudosSender;
import com.uptalent.proof.kudos.model.response.UpdatedProofKudos;
import com.uptalent.proof.model.enums.ProofStatus;
import com.uptalent.proof.model.request.ProofModify;
import com.uptalent.proof.model.response.ProofDetailInfo;
import com.uptalent.proof.model.response.ProofGeneralInfo;
import com.uptalent.proof.service.ProofService;
import com.uptalent.util.annotation.EnumValue;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1")
@Validated
@Tag(name = "Proof", description = "Proof APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class ProofController {
    private final ProofService proofService;


   @Operation(
           summary = "Retrieve list of proofs",
           description = "As a guest, I want to be able to view Proof as a list.")
   @ApiResponses({
           @ApiResponse(responseCode = "200",
                   content = { @Content(schema = @Schema(implementation = ProofGeneralInfo.class),
                           mediaType = "application/json") }),
           @ApiResponse(responseCode = "400", description = "Illegal query params",
                   content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                           mediaType = "application/json") })})
   @GetMapping("/proofs")
   @ResponseStatus(HttpStatus.OK)
   public PageWithMetadata<? extends ProofGeneralInfo> getAllProofs(
           @Min(value = 0, message = "Page should be greater or equals 0")
           @RequestParam(defaultValue = "0") int page,
           @Positive(message = "Size should be positive")
           @RequestParam(defaultValue = "9") int size,
           @RequestParam(defaultValue = "desc") String sort) {
       return proofService.getProofs(page, size, sort);
   }


    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve list of proofs from talent profile",
            description = "As a talent, I want my Proofs to be displayed on my profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = ProofDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Illegal query params",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot list of proof " +
                    "which has not PUBLISHED status from other talent profile",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @GetMapping("/talents/{talent-id}/proofs")
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<? extends ProofDetailInfo> getAllTalentProofs(
            @Min(value = 0, message = "Page should be greater or equals 0")
            @RequestParam(defaultValue = "0") int page,
            @Positive(message = "Size should be positive")
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "published")
            @EnumValue(enumClass = ProofStatus.class) String status,
            @RequestParam(defaultValue = "desc") String sort,
            @PathVariable("talent-id") Long talentId) {
        return proofService.getTalentProofs(page, size, sort, talentId, status);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve proof detail",
            description = "As a talent, I want to get proof detail.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = ProofDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Illegal query params",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot get proof detail from other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent or Proof by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @GetMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.OK)
    public ProofDetailInfo getProofDetailInfo(@PathVariable Long talentId,
                                              @PathVariable Long proofId) {
        return proofService.getProofDetailInfo(talentId, proofId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Edit/Publish/Hide/Reopen proof",
            description = "As a talent, I want to be able to Edit/Publish/Hide/Reopen proof")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = ProofDetailInfo.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot update proof for other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent or Proof by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Illegal operation",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PatchMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.OK)
    public ProofDetailInfo editProof(@Valid @RequestBody ProofModify proofModify,
                                     @PathVariable Long talentId,
                                     @PathVariable Long proofId) {
        return proofService.editProof(proofModify, talentId, proofId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create proof",
            description = "As a talent, I want to be able to create proof")
    @ApiResponses({
            @ApiResponse(responseCode = "201", headers = {@Header(name = "location", description = "proof-id")}),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot add proof for other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PostMapping("/talents/{talentId}/proofs")
    public ResponseEntity<?> createProof(@Valid @RequestBody ProofModify proofModify,
                                         @PathVariable Long talentId) {
        URI proofLocation = proofService.createProof(proofModify, talentId);

        return ResponseEntity.created(proofLocation).build();
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete proof",
            description = "As a talent, I want to be able to delete my own proofs")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot delete proof from other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Talent or Proof by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @DeleteMapping("/talents/{talentId}/proofs/{proofId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteProof(@PathVariable Long proofId,
                                         @PathVariable Long talentId) {
        proofService.deleteProof(proofId, talentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get data who posted kudos on proof",
            description = "As a talent, i want to be able to see a list of those who put kudos only on my proofs")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = KudosSender.class),
                    mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "403", description = "You cannot get list of those," +
                    " who posted kudos on proof from other talent",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Proof by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @GetMapping("/proofs/{proofId}/kudos")
    @ResponseStatus(HttpStatus.OK)
    public List<KudosSender> getKudosSenders(@PathVariable Long proofId) {
        return proofService.getKudosSenders(proofId);
    }


    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Post kudos to proof",
            description = "As a talent, I want to be able to put the kudos to proofs of another talents")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "No content",
                    content = { @Content(schema = @Schema(implementation = UpdatedProofKudos.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "400", description = "Invalid fields",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Log in to get access to the page",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "404", description = "Proof by id was not found",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "409", description = "Illegal posting kudos",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PostMapping("/proofs/{proofId}/kudos")
    @ResponseStatus(HttpStatus.OK)
    public UpdatedProofKudos postKudos(@PathVariable Long proofId,
                                       @RequestBody PostKudos postKudos) {
        return proofService.postKudos(postKudos, proofId);
    }

}

