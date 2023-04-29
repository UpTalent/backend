package com.uptalent.auth.controller;

import com.uptalent.auth.model.request.AuthLogin;
import com.uptalent.auth.model.response.AuthResponse;
import com.uptalent.auth.service.AuthService;
import com.uptalent.payload.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth APIs documentation")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Guest log in",
            description = "As a guest, I want to log in on the site",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = AuthLogin.class),
                            mediaType = "application/json")))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    content = { @Content(schema = @Schema(implementation = AuthResponse.class),
                            mediaType = "application/json") }),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = { @Content(schema = @Schema(implementation = HttpResponse.class),
                            mediaType = "application/json") })})
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthLogin authLogin) {
        return authService.login(authLogin);
    }
}
