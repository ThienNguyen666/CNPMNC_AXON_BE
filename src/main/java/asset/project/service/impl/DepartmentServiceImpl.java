package asset.project.service.impl;

import asset.project.dto.request.DepartmentReq;
import asset.project.dto.response.DepartmentRes;
import asset.project.entity.Department;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.DepartmentRepository;
import asset.project.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public List<DepartmentRes> getAll() {
        return departmentRepository.findAllByOrderByNameAsc().stream().map(this::toRes).toList();
    }

    @Override
    public DepartmentRes getById(UUID id) {
        return toRes(findOrThrow(id));
    }

    @Override
    @Transactional
    public DepartmentRes create(DepartmentReq req) {
        if (departmentRepository.existsByCode(req.code())) {
            throw new BusinessException("Department code already exists: " + req.code());
        }
        Department dept = Department.builder()
                .name(req.name())
                .code(req.code().toUpperCase())
                .build();
        return toRes(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public DepartmentRes update(UUID id, DepartmentReq req) {
        Department dept = findOrThrow(id);
        dept.setName(req.name());
        dept.setCode(req.code().toUpperCase());
        return toRes(departmentRepository.save(dept));
    }

    private Department findOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private DepartmentRes toRes(Department d) {
        return new DepartmentRes(d.getId(), d.getName(), d.getCode());
    }
}