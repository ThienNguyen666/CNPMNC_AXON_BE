package asset.project.controller;

import asset.project.dto.request.OpenValidationSessionReq;
import asset.project.dto.request.ValidationStatusReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.ValidationRecordRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.enums.ValidationRecordStatus;
import asset.project.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @GetMapping("/sessions")
    public ApiResponse<List<ValidationSessionRes>> getSessions() {
        return ApiResponse.success(validationService.getAllSessions());
    }

    @GetMapping("/sessions/{id}")
    public ApiResponse<ValidationSessionRes> getSession(@PathVariable UUID id) {
        return ApiResponse.success(validationService.getSessionById(id));
    }

    @PostMapping("/sessions")
    public ApiResponse<ValidationSessionRes> openSession(@Valid @RequestBody OpenValidationSessionReq req) {
        return ApiResponse.created(validationService.openSession(req));
    }

    @PutMapping("/sessions/{id}/close")
    public ApiResponse<Void> closeSession(@PathVariable UUID id) {
        validationService.closeSession(id);
        return ApiResponse.ok("Session closed");
    }

    @GetMapping("/sessions/{id}/records")
    public ApiResponse<List<ValidationRecordRes>> getRecords(
            @PathVariable UUID id,
            @RequestParam(required = false) ValidationRecordStatus status,
            @RequestParam(required = false) UUID departmentId) {
        return ApiResponse.success(validationService.getRecords(id, status, departmentId));
    }

    @PutMapping("/assets/{assetId}/status")
    public ApiResponse<Void> submitStatus(@PathVariable UUID assetId,
            @Valid @RequestBody ValidationStatusReq req) {
        validationService.submitValidationStatus(assetId, req);
        return ApiResponse.ok("Validation status updated");
    }
}