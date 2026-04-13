package asset.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentReq(
    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    String name,

    @NotBlank(message = "Department code is required")
    @Size(max = 20)
    String code
) {}