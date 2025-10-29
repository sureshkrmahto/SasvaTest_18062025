package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final JwtService jwtService;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and receive JWT token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "developer@company.com");
        // MVP: accept any non-empty password; do not store users
        String token = jwtService.generateToken(username, 3600);
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true;
        res.data = Map.of(
                "token", token,
                "expires_in", 3600,
                "token_type", "Bearer",
                "user", Map.of(
                        "id", "user_" + Math.abs(username.hashCode()),
                        "username", username,
                        "role", "developer",
                        "permissions", new String[]{"project:read","project:analyze","migration:execute"}
                )
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refresh(@RequestBody Map<String, String> body) {
        String token = jwtService.generateToken("developer@company.com", 3600);
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true;
        res.data = Map.of(
                "token", token,
                "expires_in", 3600,
                "token_type", "Bearer"
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout() {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true;
        res.data = Map.of("message", "Successfully logged out");
        return ResponseEntity.ok(res);
    }
}
