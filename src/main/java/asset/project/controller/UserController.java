// package asset.project.controller;

// import asset.project.dto.request.AssignRoleReq;
// import asset.project.dto.request.UpdateUserStatusReq;
// import asset.project.dto.response.ApiResponse;
// import asset.project.dto.response.PageRes;
// import asset.project.dto.response.UserRes;
// import asset.project.enums.UserRole;
// import asset.project.service.UserService;
// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.web.bind.annotation.*;

// import java.util.UUID;

// @RestController
// @RequestMapping("/api/users")
// @RequiredArgsConstructor
// public class UserController {

//     private final UserService userService;

//     @GetMapping("/me")
//     public ApiResponse<UserRes> getMe() {
//         return ApiResponse.success(userService.getMe());
//     }

//     @GetMapping
//     public ApiResponse<PageRes<UserRes>> getAll(
//             @RequestParam(required = false) UserRole role,
//             @RequestParam(required = false) Boolean isActive,
//             @RequestParam(required = false) String search,
//             @RequestParam(defaultValue = "0") int page,
//             @RequestParam(defaultValue = "10") int size) {
//         return ApiResponse.success(userService.getAll(role, isActive, search, PageRequest.of(page, size)));
//     }

//     @GetMapping("/{id}")
//     public ApiResponse<UserRes> getById(@PathVariable UUID id) {
//         return ApiResponse.success(userService.getById(id));
//     }

//     @PutMapping("/{id}/role")
//     public ApiResponse<Void> assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleReq req) {
//         userService.assignRole(id, req);
//         return ApiResponse.ok("Role updated");
//     }

//     @PutMapping("/{id}/status")
//     public ApiResponse<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusReq req) {
//         userService.updateStatus(id, req);
//         return ApiResponse.ok("Status updated");
//     }
// }

package asset.project.controller;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.CreateUserReq;
import asset.project.dto.request.UpdateDepartmentReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.PageRes;
import asset.project.dto.response.UserRes;
import asset.project.enums.UserRole;
import asset.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserRes> getMe() {
        return ApiResponse.success(userService.getMe());
    }

    @GetMapping
    public ApiResponse<PageRes<UserRes>> getAll(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getAll(role, isActive, search, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserRes> getById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getById(id));
    }

    /** Admin creates a new account with optional role and department */
    @PostMapping
    public ApiResponse<UserRes> createUser(@Valid @RequestBody CreateUserReq req) {
        return ApiResponse.created(userService.createUser(req));
    }

    @PutMapping("/{id}/role")
    public ApiResponse<Void> assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleReq req) {
        userService.assignRole(id, req);
        return ApiResponse.ok("Role updated");
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusReq req) {
        userService.updateStatus(id, req);
        return ApiResponse.ok("Status updated");
    }

    /** Admin moves a user to a different department (or removes them with null) */
    @PutMapping("/{id}/department")
    public ApiResponse<Void> updateDepartment(@PathVariable UUID id, @RequestBody UpdateDepartmentReq req) {
        userService.updateDepartment(id, req);
        return ApiResponse.ok("Department updated");
    }
}