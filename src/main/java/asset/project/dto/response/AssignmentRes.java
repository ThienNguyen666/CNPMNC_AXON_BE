package asset.project.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AssignmentRes(
    UUID id,
    UUID assetId,
    String assetCode,
    String assetName,
    UUID departmentId,
    String departmentName,
    UUID assignedById,
    String assignedByName,
    OffsetDateTime assignedAt,
    OffsetDateTime returnedAt,
    String notes
) {}