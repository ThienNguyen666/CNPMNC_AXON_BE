package asset.project.controller;

import asset.project.dto.request.DepartmentReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.DepartmentRes;
import asset.project.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ApiResponse<List<DepartmentRes>> getAll() {
        return ApiResponse.success(departmentService.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DepartmentRes> getById(@PathVariable UUID id) {
        return ApiResponse.success(departmentService.getById(id));
    }

    @PostMapping
    public ApiResponse<DepartmentRes> create(@Valid @RequestBody DepartmentReq req) {
        return ApiResponse.created(departmentService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentRes> update(@PathVariable UUID id, @Valid @RequestBody DepartmentReq req) {
        return ApiResponse.success(departmentService.update(id, req));
    }
}