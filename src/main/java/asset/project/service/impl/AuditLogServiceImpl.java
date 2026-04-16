package asset.project.service.impl;

import asset.project.dto.response.AuditLogRes;
import asset.project.dto.response.PageRes;
import asset.project.entity.Asset;
import asset.project.entity.AuditLog;
import asset.project.entity.User;
import asset.project.enums.AuditAction;
import asset.project.repository.AuditLogRepository;
import asset.project.repository.AuditLogSpec;
import asset.project.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public PageRes<AuditLogRes> getAll(AuditAction action, UUID assetId, UUID performedById,
                                        OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
        var spec = AuditLogSpec.build(action, assetId, performedById, from, to);
        return PageRes.from(auditLogRepository.findAll(spec, pageable).map(this::toRes));
    }

    @Override
    public List<AuditLogRes> getByAsset(UUID assetId) {
        return auditLogRepository.findByAssetIdOrderByCreatedAtDesc(assetId)
                .stream().map(this::toRes).toList();
    }

    @Override
    public void log(AuditAction action, User performedBy, Asset asset, User targetUser,
                    Map<String, Object> before, Map<String, Object> after, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .asset(asset)
                .targetUser(targetUser)
                .beforeState(before)
                .afterState(after)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    private AuditLogRes toRes(AuditLog l) {
        return new AuditLogRes(l.getId(), l.getAction(),
                l.getPerformedBy() != null ? l.getPerformedBy().getId() : null,
                l.getPerformedBy() != null ? l.getPerformedBy().getFullName() : null,
                l.getAsset() != null ? l.getAsset().getId() : null,
                l.getAsset() != null ? l.getAsset().getAssetCode() : null,
                l.getTargetUser() != null ? l.getTargetUser().getId() : null,
                l.getTargetUser() != null ? l.getTargetUser().getFullName() : null,
                l.getBeforeState(), l.getAfterState(), l.getIpAddress(), l.getCreatedAt());
    }
}