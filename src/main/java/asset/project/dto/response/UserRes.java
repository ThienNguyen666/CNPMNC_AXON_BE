package asset.project.dto.response;

import asset.project.enums.UserRole;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserRes(
    UUID id,
    String email,
    String fullName,
    UserRole role,
    UUID departmentId,
    String departmentName,
    boolean isActive,
    OffsetDateTime createdAt
) {}