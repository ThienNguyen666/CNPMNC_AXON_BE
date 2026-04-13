package asset.project.dto.request;

import asset.project.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record AssignRoleReq(
    @NotNull(message = "Role is required")
    UserRole role
) {}