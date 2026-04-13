package asset.project.controller;

import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.AuditLogRes;
import asset.project.dto.response.PageRes;
import asset.project.enums.AuditAction;
import asset.project.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ApiResponse<PageRes<AuditLogRes>> getAll(
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false) UUID performedById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(auditLogService.getAll(action, assetId, performedById, from, to, PageRequest.of(page, size)));
    }

    @GetMapping("/assets/{assetId}")
    public ApiResponse<List<AuditLogRes>> getByAsset(@PathVariable UUID assetId) {
        return ApiResponse.success(auditLogService.getByAsset(assetId));
    }
}