package com.moodplaylist.presentation.auth;

import com.moodplaylist.common.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    @PostMapping("/social/{provider}")
    public ApiResponse<Map<String, String>> socialLogin(
            @PathVariable String provider,
            @Valid @RequestBody SocialLoginRequest request
    ) {
        return ApiResponse.ok(Map.of(
                "provider", provider,
                "accessToken", "TODO_ACCESS_TOKEN",
                "refreshToken", "TODO_REFRESH_TOKEN"
        ));
    }

    @PostMapping("/refresh")
    public ApiResponse<Map<String, String>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(Map.of(
                "accessToken", "TODO_NEW_ACCESS_TOKEN",
                "refreshToken", "TODO_NEW_REFRESH_TOKEN"
        ));
    }

    @PostMapping("/logout")
    public ApiResponse<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.ok(Map.of("result", "logged_out"));
    }

    public record SocialLoginRequest(@NotBlank String idToken, String accessToken) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}
}
