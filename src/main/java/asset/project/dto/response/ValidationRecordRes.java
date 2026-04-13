package asset.project.dto.response;

import asset.project.enums.ValidationRecordStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ValidationRecordRes(
    UUID id,
    UUID sessionId,
    UUID assetId,
    String assetCode,
    String assetName,
    String departmentName,
    ValidationRecordStatus status,
    UUID validatedById,
    String validatedByName,
    OffsetDateTime validatedAt,
    String notes
) {}