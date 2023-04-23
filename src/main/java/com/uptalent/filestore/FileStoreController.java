package com.uptalent.filestore;

import com.uptalent.payload.HttpResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Images APIs documentation")
@SecurityScheme(
        name = "bearerAuth",
        scheme = "bearer",
        bearerFormat = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER
)
public class FileStoreController {
    private final FileStoreService fileStoreService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Image upload to talent's profile",
            description = "As a talent, I would like to be able to change the photo or banner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") }) })
    @PostMapping(
            path = "/{id}/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    public void uploadImage(@PathVariable Long id,
                            @Parameter(required = true, description = "Image file")
                            @RequestParam
                            MultipartFile image,
                            @Parameter(description = "Operation for uploading AVATAR or BANNER")
                            @RequestParam(required = false, defaultValue = "AVATAR")
                                FileStoreOperation operation){
        fileStoreService.uploadImage(id, image, operation);
    }
}
