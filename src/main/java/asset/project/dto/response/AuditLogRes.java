package asset.project.dto.response;

import asset.project.enums.AuditAction;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogRes(
    UUID id,
    AuditAction action,
    UUID performedById,
    String performedByName,
    UUID assetId,
    String assetCode,
    UUID targetUserId,
    String targetUserName,
    Map<String, Object> beforeState,
    Map<String, Object> afterState,
    String ipAddress,
    OffsetDateTime createdAt
) {}