package asset.project.dto.response;

import asset.project.enums.UserRole;

import java.util.UUID;

public record LoginRes(
    String accessToken,
    String refreshToken,
    UserInfo user
) {
    public record UserInfo(UUID id, String email, String fullName, UserRole role) {}
}