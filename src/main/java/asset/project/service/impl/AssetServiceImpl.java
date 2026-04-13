package asset.project.service.impl;

import asset.project.dto.request.AssetCreateReq;
import asset.project.dto.request.AssetTransferReq;
import asset.project.dto.request.AssetUpdateReq;
import asset.project.dto.response.AssetDetailRes;
import asset.project.dto.response.AssetRes;
import asset.project.dto.response.AssignmentRes;
import asset.project.dto.response.PageRes;
import asset.project.entity.Asset;
import asset.project.entity.AssetAssignment;
import asset.project.entity.Department;
import asset.project.entity.User;
import asset.project.enums.AssetCategory;
import asset.project.enums.AssetStatus;
import asset.project.enums.AuditAction;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.AssetAssignmentRepository;
import asset.project.repository.AssetRepository;
import asset.project.repository.DepartmentRepository;
import asset.project.service.AssetService;
import asset.project.service.AuditLogService;
import asset.project.utils.AssetCodeGenerator;
import asset.project.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;
    private final AssetAssignmentRepository assignmentRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final AssetCodeGenerator codeGenerator;
    private final SecurityUtils securityUtils;

    @Override
    public PageRes<AssetRes> getAll(UUID departmentId, AssetStatus status, AssetCategory category,
                                     String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            search = "";
        }
        return PageRes.from(
                assetRepository.findAllFiltered(departmentId, status, category, search, pageable)
                        .map(this::toRes));
    }

    @Override
    public AssetDetailRes getById(UUID id) {
        Asset a = findOrThrow(id);
        return toDetailRes(a);
    }

    @Override
    @Transactional
    public UUID create(AssetCreateReq req) {
        User actor = securityUtils.getCurrentUser();
        Department dept = req.departmentId() != null
                ? departmentRepository.findById(req.departmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()))
                : null;

        String code = codeGenerator.generate(dept != null ? dept.getCode() : "GEN");

        Asset asset = Asset.builder()
                .assetCode(code)
                .name(req.name())
                .description(req.description())
                .category(req.category())
                .status(req.status())
                .purchasePrice(req.purchasePrice())
                .purchaseDate(req.purchaseDate())
                .currentDepartment(dept)
                .createdBy(actor)
                .build();

        asset = assetRepository.save(asset);

        if (dept != null) {
            AssetAssignment assignment = AssetAssignment.builder()
                    .asset(asset)
                    .department(dept)
                    .assignedBy(actor)
                    .assignedAt(OffsetDateTime.now())
                    .notes("Initial assignment")
                    .build();
            assignmentRepository.save(assignment);
        }

        auditLogService.log(AuditAction.asset_created, actor, asset, null, null,
                Map.of("assetCode", code), null);

        return asset.getId();
    }

    @Override
    @Transactional
    public void update(UUID id, AssetUpdateReq req) {
        Asset asset = findOrThrow(id);
        Map<String, Object> before = Map.of("name", asset.getName(), "status", asset.getStatus());

        asset.setName(req.name());
        asset.setDescription(req.description());
        asset.setCategory(req.category());
        asset.setStatus(req.status());
        asset.setPurchasePrice(req.purchasePrice());
        asset.setPurchaseDate(req.purchaseDate());

        if (req.departmentId() != null) {
            Department dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
            asset.setCurrentDepartment(dept);
        }

        assetRepository.save(asset);
        auditLogService.log(AuditAction.asset_updated, securityUtils.getCurrentUser(), asset, null,
                before, Map.of("name", req.name(), "status", req.status()), null);
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        Asset asset = findOrThrow(id);
        if (asset.getStatus() == AssetStatus.archived) {
            throw new BusinessException("Asset is already archived");
        }
        asset.setStatus(AssetStatus.archived);
        asset.setArchivedAt(OffsetDateTime.now());
        assetRepository.save(asset);
        auditLogService.log(AuditAction.asset_archived, securityUtils.getCurrentUser(), asset, null,
                Map.of("status", "active"), Map.of("status", "archived"), null);
    }

    @Override
    @Transactional
    public void transfer(UUID id, AssetTransferReq req) {
        Asset asset = findOrThrow(id);
        Department newDept = departmentRepository.findById(req.newDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", req.newDepartmentId()));

        // Close current open assignment if exists
        assignmentRepository.findOpenAssignment(id).ifPresent(a -> {
            a.setReturnedAt(OffsetDateTime.now());
            assignmentRepository.save(a);
        });

        User actor = securityUtils.getCurrentUser();
        String oldDeptName = asset.getCurrentDepartment() != null
                ? asset.getCurrentDepartment().getName() : "none";

        asset.setCurrentDepartment(newDept);
        assetRepository.save(asset);

        AssetAssignment newAssignment = AssetAssignment.builder()
                .asset(asset)
                .department(newDept)
                .assignedBy(actor)
                .assignedAt(OffsetDateTime.now())
                .notes(req.notes())
                .build();
        assignmentRepository.save(newAssignment);

        auditLogService.log(AuditAction.asset_transferred, actor, asset, null,
                Map.of("department", oldDeptName),
                Map.of("department", newDept.getName()), null);
    }

    @Override
    @Transactional
    public void returnAsset(UUID id) {
        findOrThrow(id);
        AssetAssignment open = assignmentRepository.findOpenAssignment(id)
                .orElseThrow(() -> new BusinessException("No active assignment found for asset"));
        open.setReturnedAt(OffsetDateTime.now());
        assignmentRepository.save(open);
    }

    @Override
    public List<AssignmentRes> getHistory(UUID id) {
        findOrThrow(id);
        return assignmentRepository.findByAssetIdOrderByAssignedAtDesc(id)
                .stream().map(this::toAssignmentRes).toList();
    }

    private Asset findOrThrow(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id));
    }

    private AssetRes toRes(Asset a) {
        return new AssetRes(a.getId(), a.getAssetCode(), a.getName(), a.getDescription(),
                a.getCategory(), a.getStatus(), a.getPurchasePrice(), a.getPurchaseDate(),
                a.getCurrentDepartment() != null ? a.getCurrentDepartment().getId() : null,
                a.getCurrentDepartment() != null ? a.getCurrentDepartment().getName() : null,
                a.getCreatedAt());
    }

    private AssetDetailRes toDetailRes(Asset a) {
        return new AssetDetailRes(a.getId(), a.getAssetCode(), a.getName(), a.getDescription(),
                a.getCategory(), a.getStatus(), a.getPurchasePrice(), a.getPurchaseDate(),
                a.getCurrentDepartment() != null ? a.getCurrentDepartment().getId() : null,
                a.getCurrentDepartment() != null ? a.getCurrentDepartment().getName() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getFullName() : null,
                a.getArchivedAt(), a.getCreatedAt(), a.getUpdatedAt());
    }

    private AssignmentRes toAssignmentRes(AssetAssignment aa) {
        return new AssignmentRes(aa.getId(),
                aa.getAsset().getId(), aa.getAsset().getAssetCode(), aa.getAsset().getName(),
                aa.getDepartment().getId(), aa.getDepartment().getName(),
                aa.getAssignedBy() != null ? aa.getAssignedBy().getId() : null,
                aa.getAssignedBy() != null ? aa.getAssignedBy().getFullName() : null,
                aa.getAssignedAt(), aa.getReturnedAt(), aa.getNotes());
    }
}