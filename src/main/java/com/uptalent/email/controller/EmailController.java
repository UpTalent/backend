package com.uptalent.email.controller;

import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.email.service.EmailService;
import com.uptalent.payload.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email")
@RequiredArgsConstructor
@Validated
@Tag(name = "Email", description = "Email APIs documentation")
public class EmailController {

    private final EmailService emailService;

    @Operation(
            summary = "User restore account",
            description = "As a user, I want to restore account in 7 days")
    @ApiResponses({
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", description = "Invalid token",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })
    })
    @PostMapping("/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreUser(@RequestParam String token) {
        emailService.restoreAccount(token);
    }

    @Operation(
            summary = "User verify account",
            description = "As a user, I want to verify account in 7 days")
    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "Invalid token",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") })
    })
    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse verifyUser(@RequestParam String token) {
        return emailService.verifyAccount(token);
    }


}
