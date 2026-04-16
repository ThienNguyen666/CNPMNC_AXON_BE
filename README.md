## Cho dev dùng VSCode
Dùng lệnh này chạy trong terminal để start app

```
Get-Content .env | ForEach-Object { if ($_ -match "^(.*?)=(.*)$") { [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2]) } }; ./mvnw spring-boot:run
```

## Dev dùng IntelJ
Nạp biến env vào IntelJ


## Test plan được trình bày trong TestPlan.md

# TÀI LIỆU THIẾT KẾ BACKEND - ASSET MANAGEMENT SYSTEM (AMS)

## Cấu Trúc Thư Mục (Folder Structure)

Kiến trúc phân lớp chuẩn cho Spring Boot 3 (Java 21):

```text
src/main/java/asset/project/
 ├── config/            # Cấu hình Security, JWT, CORS, OpenAPI/Swagger
 ├── controller/        # REST Controllers (Định nghĩa các Endpoints)
 ├── dto/               
 │   ├── request/       # DTO nhận dữ liệu đầu vào (Ví dụ: LoginReq, AssetCreateReq)
 │   └── response/      # DTO trả dữ liệu ra (Ví dụ: ApiResponse, AssetRes, UserRes)
 ├── entity/            # JPA Entities (Mapping với Database)
 ├── exception/         # Xử lý lỗi tập trung (@RestControllerAdvice)
 ├── repository/        # Spring Data JPA Repositories
 ├── service/           # Interfaces chứa Business Logic
 │   └── impl/          # Implementations của các Service interfaces
 └── utils/             # Các lớp tiện ích (SecurityUtils, DateUtils)

## API Specifications (Chi tiết Input / Output)

Quy ước: Tất cả API thành công đều được bọc trong một class ApiResponse<T> thống nhất để Frontend dễ xử lý.

Cấu trúc Response chuẩn:

```json
{
  "status": 200,
  "message": "Success",
  "data": { ... } // Hoặc null nếu không có data
}
```

### Authentication & User Management

#### Đăng nhập hệ thống

**Endpoint:** POST /api/auth/login

**Input (Request Body):**

```json
{
  "email": "admin@company.com",
  "password": "Password123!"
}
```

**Output (Response Data):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid-string",
    "email": "admin@company.com",
    "role": "ADMIN"
  }
}
```

---

#### Lấy thông tin User hiện tại

**Endpoint:** GET /api/users/me

**Input:** Header `Authorization: Bearer <token>`

**Output:** Chi tiết User đang đăng nhập.

---

#### Khóa/Mở khóa tài khoản nhân viên (Admin)

**Endpoint:** PUT /api/users/{id}/status

**Input (Path Variable):** id (UUID của user)

**Input (Request Body):**

```json
{ "isActive": false }
```

**Output:** 200 OK

---

### Department (Phòng ban)

#### Lấy danh sách phòng ban (Dùng cho Dropdown)

**Endpoint:** GET /api/departments

**Input:** Không

**Output:**

```json
[
  { "id": "uuid-1", "name": "Engineering" },
  { "id": "uuid-2", "name": "Sales" }
]
```

---

### Asset Management (Quản lý tài sản)

#### Lấy danh sách tài sản (Kèm Lọc & Phân trang)

**Endpoint:** GET /api/assets

**Input (Query Params):**
- page (int, default=0)
- size (int, default=10)
- search (string, optional - tìm theo tên/mã)
- departmentId (UUID, optional)
- type (string, optional)
- status (string, optional)

**Output (Response Data):**

```json
{
  "content": [
    {
      "id": "uuid-string",
      "assetCode": "#AST-00142",
      "name": "Dell XPS 15 Laptop",
      "type": "Hardware",
      "departmentName": "Engineering",
      "location": "Floor 3 - A12",
      "purchaseDate": "2024-01-14",
      "status": "VALID"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 1284,
  "totalPages": 129
}
```

---

#### Tạo tài sản mới

**Endpoint:** POST /api/assets

**Input (Request Body):**

```json
{
  "name": "MacBook Pro M3",
  "type": "Hardware",
  "value": 2000.0,
  "purchaseDate": "2024-04-10",
  "status": "PENDING",
  "location": "Kho IT",
  "departmentId": "uuid-of-department" // Optional
}
```

**Output (Response Data):** Trả về ID của tài sản vừa tạo "uuid-string"

---

#### Cập nhật tài sản

**Endpoint:** PUT /api/assets/{id}

**Input:** Tương tự Create Request.

**Output:** 200 OK

---

### Asset Assignment (Luân chuyển)

#### Gán / Chuyển phòng ban cho tài sản

**Endpoint:** POST /api/assets/{id}/transfer

**Input (Request Body):**

```json
{
  "newDepartmentId": "uuid-string",
  "reason": "Chuyển thiết bị cho nhân viên mới",
  "location": "Floor 2 - B01" // Vị trí mới
}
```

**Output:** 200 OK

---

#### Lấy lịch sử luân chuyển của 1 tài sản

**Endpoint:** GET /api/assets/{id}/history

**Output:** Danh sách các AssetHistory trả về mảng.

---

### Validation / Audit (Kiểm kê)

#### Submit kết quả kiểm kê cho tài sản (Department Staff)

**Endpoint:** PUT /api/assets/{id}/validation-status

**Input (Request Body):**

```json
{
  "status": "VALID", // VALID, INVALID, MISSING
  "note": "Máy hoạt động bình thường"
}
```

**Output:** 200 OK

---

### Dashboard

#### Thống kê tổng quan (Widget KPIs)

**Endpoint:** GET /api/dashboard/stats

**Output:**

```json
{
  "totalAssets": 1284,
  "validAssets": 987,
  "invalidAssets": 184,
  "missingAssets": 113,
  "pendingReviews": 21
}
```

## Prototype Classes & Code Backend

Dưới đây là thiết kế code cụ thể cho việc triển khai chuẩn.

### Standard API Response Wrapper
Tạo một class dùng chung để gói mọi response.

```java
package asset.project.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("Success")
                .data(data)
                .build();
    }
}
```

### DTOs cho Asset (Sử dụng Record của Java)
Record giúp DTO ngắn gọn, an toàn (immutable) và tự động có getter.

```java
package asset.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.UUID;

public record AssetCreateReq(
    @NotBlank(message = "Tên tài sản không được để trống") 
    String name,
    
    @NotBlank(message = "Loại tài sản không được để trống") 
    String type,
    
    Double value,
    LocalDate purchaseDate,
    String location,
    
    @NotBlank(message = "Trạng thái không được để trống") 
    String status,
    
    UUID departmentId // Có thể null
) {}

public record AssetTransferReq(
    UUID newDepartmentId,
    String reason,
    String location
) {}
```

```java
package asset.project.dto.response;

import java.time.LocalDate;
import java.util.UUID;

// Không trả Entity chứa toàn bộ Department object ra ngoài, chỉ trả những thông tin cần thiết
public record AssetRes(
    UUID id,
    String assetCode,
    String name,
    String type,
    String location,
    LocalDate purchaseDate,
    String status,
    UUID departmentId,
    String departmentName
) {}
```

### Asset Controller
Các API mapping input/output rõ ràng:

```java
package asset.project.controller;

import asset.project.dto.request.AssetCreateReq;
import asset.project.dto.request.AssetTransferReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.AssetRes;
import asset.project.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    // API: Lấy danh sách tài sản
    @GetMapping
    public ApiResponse<Page<AssetRes>> getAssets(
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        Page<AssetRes> result = assetService.getAssets(departmentId, type, status, search, page, size);
        return ApiResponse.success(result);
    }

    // API: Lấy chi tiết 1 tài sản
    @GetMapping("/{id}")
    public ApiResponse<AssetRes> getAssetById(@PathVariable UUID id) {
        return ApiResponse.success(assetService.getAssetById(id));
    }

    // API: Tạo tài sản mới
    @PostMapping
    public ApiResponse<UUID> createAsset(@Valid @RequestBody AssetCreateReq req) {
        UUID newAssetId = assetService.createAsset(req);
        return ApiResponse.success(newAssetId);
    }

    // API: Luân chuyển tài sản
    @PostMapping("/{id}/transfer")
    public ApiResponse<Void> transferAsset(
            @PathVariable UUID id, 
            @Valid @RequestBody AssetTransferReq req) {
        assetService.transferAsset(id, req);
        return ApiResponse.success(null);
    }
}
```

### Global Exception Handler
Đây là file cực kỳ quan trọng để Frontend nhận được thông báo lỗi tử tế (dưới dạng JSON) thay vì một trang HTML trắng báo lỗi 500 của Spring.

```java
package asset.project.exception;

import asset.project.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Bắt lỗi Validation (từ các tag @NotBlank, @NotNull trong DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.builder()
                .status(400)
                .message(errorMsg)
                .data(null)
                .build();
    }

    // Bắt lỗi logic nghiệp vụ chung
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> handleRuntimeException(RuntimeException ex) {
        return ApiResponse.builder()
                .status(400)
                .message(ex.getMessage())
                .data(null)
                .build();
    }
}
```