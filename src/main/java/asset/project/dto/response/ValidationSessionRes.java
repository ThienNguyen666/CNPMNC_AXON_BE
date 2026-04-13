package asset.project.dto.response;

import asset.project.enums.ValidationSessionStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ValidationSessionRes(
    UUID id,
    short year,
    ValidationSessionStatus status,
    UUID initiatedById,
    String initiatedByName,
    OffsetDateTime startedAt,
    OffsetDateTime closedAt,
    String notes,
    long totalRecords,
    long validCount,
    long invalidCount,
    long missingCount,
    long pendingCount
) {}