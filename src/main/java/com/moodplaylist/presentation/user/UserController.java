package com.moodplaylist.presentation.user;

import com.moodplaylist.common.api.ApiResponse;
import com.moodplaylist.infrastructure.persistence.repository.JpaUserRepository;
import com.moodplaylist.presentation.auth.AuthUserResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final JpaUserRepository userRepository;

    public UserController(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long userId = AuthUserResolver.currentUserIdOrThrow();
        var user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("user not found"));
        return ApiResponse.ok(Map.of("id", user.getId(), "email", user.getEmail(), "nickname", user.getNickname()));
    }
}
