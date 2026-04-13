package asset.project.controller;

import asset.project.dto.request.AssetCreateReq;
import asset.project.dto.request.AssetTransferReq;
import asset.project.dto.request.AssetUpdateReq;
import asset.project.dto.request.ValidationStatusReq;
import asset.project.dto.response.*;
import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;
import asset.project.service.AssetService;
import asset.project.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final ValidationService validationService;

    @GetMapping
    public ApiResponse<PageRes<AssetRes>> getAll(
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) AssetCategory category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(assetService.getAll(departmentId, status, category, search, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<AssetDetailRes> getById(@PathVariable UUID id) {
        return ApiResponse.success(assetService.getById(id));
    }

    @PostMapping
    public ApiResponse<UUID> create(@Valid @RequestBody AssetCreateReq req) {
        return ApiResponse.created(assetService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable UUID id, @Valid @RequestBody AssetUpdateReq req) {
        assetService.update(id, req);
        return ApiResponse.ok("Asset updated");
    }

    @PutMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable UUID id) {
        assetService.archive(id);
        return ApiResponse.ok("Asset archived");
    }

    @PostMapping("/{id}/transfer")
    public ApiResponse<Void> transfer(@PathVariable UUID id, @Valid @RequestBody AssetTransferReq req) {
        assetService.transfer(id, req);
        return ApiResponse.ok("Asset transferred");
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Void> returnAsset(@PathVariable UUID id) {
        assetService.returnAsset(id);
        return ApiResponse.ok("Assignment closed");
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<AssignmentRes>> getHistory(@PathVariable UUID id) {
        return ApiResponse.success(assetService.getHistory(id));
    }

    @PutMapping("/{id}/validation-status")
    public ApiResponse<Void> submitValidation(@PathVariable UUID id,
            @Valid @RequestBody ValidationStatusReq req) {
        validationService.submitValidationStatus(id, req);
        return ApiResponse.ok("Validation status updated");
    }
}