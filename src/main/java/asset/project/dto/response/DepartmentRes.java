package asset.project.dto.response;

import java.util.UUID;

public record DepartmentRes(
    UUID id,
    String name,
    String code
) {}