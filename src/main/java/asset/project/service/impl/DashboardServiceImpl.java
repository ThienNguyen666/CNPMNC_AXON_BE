package asset.project.service.impl;

import asset.project.dto.response.DashboardStatsRes;
import asset.project.dto.response.DepartmentStatsRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.enums.AssetStatus;
import asset.project.enums.ValidationSessionStatus;
import asset.project.repository.AssetRepository;
import asset.project.repository.ValidationSessionRepository;
import asset.project.service.DashboardService;
import asset.project.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AssetRepository assetRepository;
    private final ValidationSessionRepository sessionRepository;
    private final ValidationService validationService;

    @Override
    public DashboardStatsRes getStats() {
        long total    = assetRepository.count();
        long active   = assetRepository.countByStatus(AssetStatus.active);
        long inactive = assetRepository.countByStatus(AssetStatus.inactive);
        long archived = assetRepository.countByStatus(AssetStatus.archived);
        long disposed = assetRepository.countByStatus(AssetStatus.disposed);

        var progress = getValidationProgress();
        return new DashboardStatsRes(total, active, inactive, archived, disposed,
                progress != null ? progress.validCount() : 0,
                progress != null ? progress.invalidCount() : 0,
                progress != null ? progress.missingCount() : 0,
                progress != null ? progress.pendingCount() : 0);
    }

    @Override
    public List<DepartmentStatsRes> getByDepartment() {
        return assetRepository.countAndSumByDepartment().stream()
                .map(row -> new DepartmentStatsRes(
                        (UUID) row[0],
                        (String) row[1],
                        (Long) row[2],
                        row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO))
                .toList();
    }

    @Override
    public ValidationSessionRes getValidationProgress() {
        return sessionRepository.findByStatus(ValidationSessionStatus.in_progress)
                .map(s -> validationService.getSessionById(s.getId()))
                .orElse(null);
    }
}