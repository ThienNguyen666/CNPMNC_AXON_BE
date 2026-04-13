package asset.project.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssetTransferReq(
    @NotNull(message = "Target department is required")
    UUID newDepartmentId,

    String notes
) {}