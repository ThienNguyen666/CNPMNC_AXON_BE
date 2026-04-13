package asset.project.controller;

import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.DashboardStatsRes;
import asset.project.dto.response.DepartmentStatsRes;
import asset.project.dto.response.ValidationSessionRes;
import asset.project.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsRes> getStats() {
        return ApiResponse.success(dashboardService.getStats());
    }

    @GetMapping("/by-department")
    public ApiResponse<List<DepartmentStatsRes>> getByDepartment() {
        return ApiResponse.success(dashboardService.getByDepartment());
    }

    @GetMapping("/validation-progress")
    public ApiResponse<ValidationSessionRes> getValidationProgress() {
        return ApiResponse.success(dashboardService.getValidationProgress());
    }
}