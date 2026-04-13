package asset.project.dto.request;

import jakarta.validation.constraints.NotNull;

public record OpenValidationSessionReq(
    @NotNull(message = "Year is required")
    Short year,

    String notes
) {}