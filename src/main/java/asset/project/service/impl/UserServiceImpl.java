// package asset.project.service.impl;

// import asset.project.dto.request.AssignRoleReq;
// import asset.project.dto.request.UpdateUserStatusReq;
// import asset.project.dto.response.PageRes;
// import asset.project.dto.response.UserRes;
// import asset.project.entity.User;
// import asset.project.enums.AuditAction;
// import asset.project.enums.UserRole;
// import asset.project.exception.ResourceNotFoundException;
// import asset.project.repository.UserRepository;
// import asset.project.service.AuditLogService;
// import asset.project.service.UserService;
// import asset.project.utils.SecurityUtils;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Map;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// public class UserServiceImpl implements UserService {

//     private final UserRepository userRepository;
//     private final AuditLogService auditLogService;
//     private final SecurityUtils securityUtils;

//     @Override
//     public UserRes getMe() {
//         return toRes(securityUtils.getCurrentUser());
//     }

//     @Override
//     public PageRes<UserRes> getAll(UserRole role, Boolean isActive, String search, Pageable pageable) {
//         if (search == null || search.isBlank()) {
//               search = "";
//           }
//         return PageRes.from(userRepository.findAllFiltered(role, isActive, search, pageable).map(this::toRes));
//     }

//     @Override
//     public UserRes getById(UUID id) {
//         return toRes(findOrThrow(id));
//     }

//     @Override
//     @Transactional
//     public void assignRole(UUID id, AssignRoleReq req) {
//         User target = findOrThrow(id);
//         UserRole oldRole = target.getRole();
//         target.setRole(req.role());
//         userRepository.save(target);

//         User actor = securityUtils.getCurrentUser();
//         auditLogService.log(AuditAction.role_assigned, actor, null, target,
//                 Map.of("role", String.valueOf(oldRole)),
//                 Map.of("role", req.role().name()), null);
//     }

//     @Override
//     @Transactional
//     public void updateStatus(UUID id, UpdateUserStatusReq req) {
//         User target = findOrThrow(id);
//         target.setActive(req.isActive());
//         userRepository.save(target);

//         User actor = securityUtils.getCurrentUser();
//         AuditAction action = req.isActive() ? AuditAction.user_created : AuditAction.user_deactivated;
//         auditLogService.log(action, actor, null, target,
//                 Map.of("isActive", !req.isActive()),
//                 Map.of("isActive", req.isActive()), null);
//     }

//     private User findOrThrow(UUID id) {
//         return userRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException("User", id));
//     }

//     private UserRes toRes(User u) {
//         return new UserRes(
//                 u.getId(), u.getEmail(), u.getFullName(), u.getRole(),
//                 u.getDepartment() != null ? u.getDepartment().getId() : null,
//                 u.getDepartment() != null ? u.getDepartment().getName() : null,
//                 u.isActive(), u.getCreatedAt()
//         );
//     }
// }

package asset.project.service.impl;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.CreateUserReq;
import asset.project.dto.request.UpdateDepartmentReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.PageRes;
import asset.project.dto.response.UserRes;
import asset.project.entity.Department;
import asset.project.entity.User;
import asset.project.enums.AuditAction;
import asset.project.enums.UserRole;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.DepartmentRepository;
import asset.project.repository.UserRepository;
import asset.project.service.AuditLogService;
import asset.project.service.UserService;
import asset.project.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRes getMe() {
        return toRes(securityUtils.getCurrentUser());
    }

    @Override
    public PageRes<UserRes> getAll(UserRole role, Boolean isActive, String search, Pageable pageable) {
        return PageRes.from(userRepository.findAllFiltered(role, isActive, search, pageable).map(this::toRes));
    }

    @Override
    public UserRes getById(UUID id) {
        return toRes(findOrThrow(id));
    }

    @Override
    @Transactional
    public UserRes createUser(CreateUserReq req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BusinessException("Email already exists: " + req.email());
        }

        Department dept = null;
        if (req.departmentId() != null) {
            dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
        }

        User user = User.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .fullName(req.fullName())
                .role(req.role())
                .department(dept)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        User actor = securityUtils.getCurrentUser();
        auditLogService.log(AuditAction.user_created, actor, null, user,
                null,
                Map.of("email", req.email(), "role", String.valueOf(req.role())),
                null);

        return toRes(user);
    }

    @Override
    @Transactional
    public void assignRole(UUID id, AssignRoleReq req) {
        User target = findOrThrow(id);
        UserRole oldRole = target.getRole();
        target.setRole(req.role());
        userRepository.save(target);

        User actor = securityUtils.getCurrentUser();
        auditLogService.log(AuditAction.role_assigned, actor, null, target,
                Map.of("role", String.valueOf(oldRole)),
                Map.of("role", req.role().name()), null);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, UpdateUserStatusReq req) {
        User target = findOrThrow(id);
        target.setActive(req.isActive());
        userRepository.save(target);

        User actor = securityUtils.getCurrentUser();
        AuditAction action = req.isActive() ? AuditAction.user_created : AuditAction.user_deactivated;
        auditLogService.log(action, actor, null, target,
                Map.of("isActive", !req.isActive()),
                Map.of("isActive", req.isActive()), null);
    }

    @Override
    @Transactional
    public void updateDepartment(UUID id, UpdateDepartmentReq req) {
        User target = findOrThrow(id);

        String oldDeptName = target.getDepartment() != null ? target.getDepartment().getName() : "none";

        if (req.departmentId() == null) {
            target.setDepartment(null);
        } else {
            Department dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", req.departmentId()));
            target.setDepartment(dept);
        }

        userRepository.save(target);

        String newDeptName = target.getDepartment() != null ? target.getDepartment().getName() : "none";
        User actor = securityUtils.getCurrentUser();
        auditLogService.log(AuditAction.role_assigned, actor, null, target,
                Map.of("department", oldDeptName),
                Map.of("department", newDeptName), null);
    }

    private User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    private UserRes toRes(User u) {
        return new UserRes(
                u.getId(), u.getEmail(), u.getFullName(), u.getRole(),
                u.getDepartment() != null ? u.getDepartment().getId() : null,
                u.getDepartment() != null ? u.getDepartment().getName() : null,
                u.isActive(), u.getCreatedAt()
        );
    }
}