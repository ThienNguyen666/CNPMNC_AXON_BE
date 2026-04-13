package asset.project.service.impl;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.PageRes;
import asset.project.dto.response.UserRes;
import asset.project.entity.User;
import asset.project.enums.AuditAction;
import asset.project.enums.UserRole;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.UserRepository;
import asset.project.service.AuditLogService;
import asset.project.service.UserService;
import asset.project.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final SecurityUtils securityUtils;

    @Override
    public UserRes getMe() {
        return toRes(securityUtils.getCurrentUser());
    }

    @Override
    public PageRes<UserRes> getAll(UserRole role, Boolean isActive, String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
              search = "";
          }
        return PageRes.from(userRepository.findAllFiltered(role, isActive, search, pageable).map(this::toRes));
    }

    @Override
    public UserRes getById(UUID id) {
        return toRes(findOrThrow(id));
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