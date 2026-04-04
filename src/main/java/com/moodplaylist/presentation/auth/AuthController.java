package com.moodplaylist.presentation.auth;

import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.application.auth.service.AuthService;
import com.moodplaylist.domain.auth.model.TokenPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/social/{provider}")
    public ApiResponse<Map<String, String>> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request
    ) {
        TokenPair pair = authService.socialLogin(provider, request.idToken());
        return ApiResponse.ok(Map.of(
                "provider", provider,
                "accessToken", pair.accessToken(),
                "refreshToken", pair.refreshToken()
        ));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenPair pair = authService.refresh(request.refreshToken());
        return ApiResponse.ok(Map.of(
                "accessToken", pair.accessToken(),
                "refreshToken", pair.refreshToken()
        ));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.ok(Map.of("result", "logged_out"));
    }

    public record SocialLoginRequest(@NotBlank String idToken, String accessToken) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
}
