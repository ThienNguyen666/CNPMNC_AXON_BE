package asset.project.dto.request;

import java.util.UUID;

public record UpdateDepartmentReq(
    UUID departmentId   // null = remove user from department
) {} 
