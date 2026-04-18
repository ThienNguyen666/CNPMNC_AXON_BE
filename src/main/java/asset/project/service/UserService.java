// package asset.project.service;

// import asset.project.dto.request.AssignRoleReq;
// import asset.project.dto.request.UpdateUserStatusReq;
// import asset.project.dto.response.PageRes;
// import asset.project.dto.response.UserRes;
// import asset.project.enums.UserRole;
// import org.springframework.data.domain.Pageable;

// import java.util.UUID;

// public interface UserService {
//     UserRes getMe();
//     PageRes<UserRes> getAll(UserRole role, Boolean isActive, String search, Pageable pageable);
//     UserRes getById(UUID id);
//     void assignRole(UUID id, AssignRoleReq req);
//     void updateStatus(UUID id, UpdateUserStatusReq req);
// }

package asset.project.service;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.CreateUserReq;
import asset.project.dto.request.UpdateDepartmentReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.PageRes;
import asset.project.dto.response.UserRes;
import asset.project.enums.UserRole;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserRes getMe();
    PageRes<UserRes> getAll(UserRole role, Boolean isActive, String search, Pageable pageable);
    UserRes getById(UUID id);
    UserRes createUser(CreateUserReq req);
    void assignRole(UUID id, AssignRoleReq req);
    void updateStatus(UUID id, UpdateUserStatusReq req);
    void updateDepartment(UUID id, UpdateDepartmentReq req);
}