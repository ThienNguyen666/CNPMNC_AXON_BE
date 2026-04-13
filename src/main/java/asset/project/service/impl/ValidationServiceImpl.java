package asset.project.service.impl;

import asset.project.dto.request.OpenValidationSessionReq;
import asset.project.dto.request.ValidationStatusReq;
import asset.project.dto.response.ValidationRecordRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.entity.ValidationRecord;
import asset.project.entity.ValidationSession;
import asset.project.enums.AuditAction;
import asset.project.enums.ValidationRecordStatus;
import asset.project.enums.ValidationSessionStatus;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.AssetRepository;
import asset.project.repository.ValidationRecordRepository;
import asset.project.repository.ValidationSessionRepository;
import asset.project.service.AuditLogService;
import asset.project.service.ValidationService;
import asset.project.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final ValidationSessionRepository sessionRepository;
    private final ValidationRecordRepository recordRepository;
    private final AssetRepository assetRepository;
    private final AuditLogService auditLogService;
    private final SecurityUtils securityUtils;

    @Override
    public List<ValidationSessionRes> getAllSessions() {
        return sessionRepository.findAllByOrderByYearDesc().stream().map(this::toSessionRes).toList();
    }

    @Override
    public ValidationSessionRes getSessionById(UUID id) {
        return toSessionRes(findSessionOrThrow(id));
    }

    @Override
    @Transactional
    public ValidationSessionRes openSession(OpenValidationSessionReq req) {
        if (sessionRepository.existsByYear(req.year())) {
            throw new BusinessException("A validation session for year " + req.year() + " already exists");
        }
        if (sessionRepository.findByStatus(ValidationSessionStatus.in_progress).isPresent()) {
            throw new BusinessException("Another session is already in progress");
        }

        var actor = securityUtils.getCurrentUser();
        ValidationSession session = ValidationSession.builder()
                .year(req.year())
                .status(ValidationSessionStatus.in_progress)
                .initiatedBy(actor)
                .startedAt(OffsetDateTime.now())
                .notes(req.notes())
                .build();
        session = sessionRepository.save(session);

        // Seed one record per active asset
        final ValidationSession savedSession = session;
        List<ValidationRecord> records = assetRepository.findAllActive().stream()
                .map(asset -> ValidationRecord.builder()
                        .session(savedSession)
                        .asset(asset)
                        .status(ValidationRecordStatus.pending)
                        .build())
                .toList();
        recordRepository.saveAll(records);

        auditLogService.log(AuditAction.validation_initiated, actor, null, null, null,
                Map.of("year", req.year()), null);

        return toSessionRes(session);
    }

    @Override
    @Transactional
    public void closeSession(UUID id) {
        ValidationSession session = findSessionOrThrow(id);
        if (session.getStatus() == ValidationSessionStatus.closed) {
            throw new BusinessException("Session is already closed");
        }
        session.setStatus(ValidationSessionStatus.closed);
        session.setClosedAt(OffsetDateTime.now());
        sessionRepository.save(session);
    }

    @Override
    public List<ValidationRecordRes> getRecords(UUID sessionId, ValidationRecordStatus status, UUID departmentId) {
        findSessionOrThrow(sessionId);
        return recordRepository.findBySessionFiltered(sessionId, status, departmentId)
                .stream().map(this::toRecordRes).toList();
    }

    @Override
    @Transactional
    public void submitValidationStatus(UUID assetId, ValidationStatusReq req) {
        var actor = securityUtils.getCurrentUser();
        ValidationRecord record = recordRepository.findActiveRecordForAsset(assetId)
                .orElseThrow(() -> new BusinessException("No active validation session or no record for this asset"));

        record.setStatus(req.status());
        record.setValidatedBy(actor);
        record.setValidatedAt(OffsetDateTime.now());
        record.setNotes(req.notes());
        recordRepository.save(record);

        auditLogService.log(AuditAction.validation_record_updated, actor, record.getAsset(), null,
                Map.of("status", "pending"), Map.of("status", req.status().name()), null);
    }

    private ValidationSession findSessionOrThrow(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ValidationSession", id));
    }

    private ValidationSessionRes toSessionRes(ValidationSession s) {
        long valid   = recordRepository.countBySessionIdAndStatus(s.getId(), ValidationRecordStatus.valid);
        long invalid = recordRepository.countBySessionIdAndStatus(s.getId(), ValidationRecordStatus.invalid);
        long missing = recordRepository.countBySessionIdAndStatus(s.getId(), ValidationRecordStatus.missing);
        long pending = recordRepository.countBySessionIdAndStatus(s.getId(), ValidationRecordStatus.pending);

        return new ValidationSessionRes(s.getId(), s.getYear(), s.getStatus(),
                s.getInitiatedBy() != null ? s.getInitiatedBy().getId() : null,
                s.getInitiatedBy() != null ? s.getInitiatedBy().getFullName() : null,
                s.getStartedAt(), s.getClosedAt(), s.getNotes(),
                valid + invalid + missing + pending, valid, invalid, missing, pending);
    }

    private ValidationRecordRes toRecordRes(ValidationRecord vr) {
        var asset = vr.getAsset();
        var dept  = asset.getCurrentDepartment();
        return new ValidationRecordRes(vr.getId(), vr.getSession().getId(),
                asset.getId(), asset.getAssetCode(), asset.getName(),
                dept != null ? dept.getName() : null,
                vr.getStatus(),
                vr.getValidatedBy() != null ? vr.getValidatedBy().getId() : null,
                vr.getValidatedBy() != null ? vr.getValidatedBy().getFullName() : null,
                vr.getValidatedAt(), vr.getNotes());
    }
}