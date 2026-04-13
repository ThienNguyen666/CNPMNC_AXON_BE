package asset.project.dto.request;

import asset.project.enums.ValidationRecordStatus;
import jakarta.validation.constraints.NotNull;

public record ValidationStatusReq(
    @NotNull(message = "Status is required")
    ValidationRecordStatus status,

    String notes
) {}