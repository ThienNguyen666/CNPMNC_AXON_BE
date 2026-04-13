package asset.project.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusReq(
    @NotNull(message = "isActive is required")
    Boolean isActive
) {}