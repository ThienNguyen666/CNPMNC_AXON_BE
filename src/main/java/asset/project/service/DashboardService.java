package asset.project.service;

import asset.project.dto.response.DashboardStatsRes;
import asset.project.dto.response.DepartmentStatsRes;
import asset.project.dto.response.ValidationSessionRes;

import java.util.List;

public interface DashboardService {
    DashboardStatsRes getStats();
    List<DepartmentStatsRes> getByDepartment();
    ValidationSessionRes getValidationProgress();
}