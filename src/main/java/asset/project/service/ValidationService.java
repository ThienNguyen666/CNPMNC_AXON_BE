package asset.project.service;

import asset.project.dto.request.OpenValidationSessionReq;
import asset.project.dto.request.ValidationStatusReq;
import asset.project.dto.response.ValidationRecordRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.enums.ValidationRecordStatus;

import java.util.List;
import java.util.UUID;

public interface ValidationService {
    List<ValidationSessionRes> getAllSessions();
    ValidationSessionRes getSessionById(UUID id);
    ValidationSessionRes openSession(OpenValidationSessionReq req);
    void closeSession(UUID id);
    List<ValidationRecordRes> getRecords(UUID sessionId, ValidationRecordStatus status, UUID departmentId);
    void submitValidationStatus(UUID assetId, ValidationStatusReq req);
}