package asset.project.controller;

import asset.project.dto.request.LoginReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.LoginRes;
import asset.project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.success(authService.login(req));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginRes> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return ApiResponse.success(authService.refreshToken(refreshToken));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ApiResponse.ok("Logged out successfully");
    }
}