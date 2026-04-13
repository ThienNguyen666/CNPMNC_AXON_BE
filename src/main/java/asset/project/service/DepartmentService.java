package asset.project.service;

import asset.project.dto.request.DepartmentReq;
import asset.project.dto.response.DepartmentRes;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {
    List<DepartmentRes> getAll();
    DepartmentRes getById(UUID id);
    DepartmentRes create(DepartmentReq req);
    DepartmentRes update(UUID id, DepartmentReq req);
}