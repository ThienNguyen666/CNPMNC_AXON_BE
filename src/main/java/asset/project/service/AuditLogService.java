package asset.project.service;

import asset.project.dto.response.AuditLogRes;
import asset.project.dto.response.PageRes;
import asset.project.entity.Asset;
import asset.project.entity.User;
import asset.project.enums.AuditAction;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AuditLogService {
    PageRes<AuditLogRes> getAll(AuditAction action, UUID assetId, UUID performedById,
                                OffsetDateTime from, OffsetDateTime to, Pageable pageable);
    List<AuditLogRes> getByAsset(UUID assetId);
    void log(AuditAction action, User performedBy, Asset asset, User targetUser,
             Map<String, Object> before, Map<String, Object> after, String ipAddress);
}