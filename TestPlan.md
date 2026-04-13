# Test Plan — Asset Management System (AMS) Backend

**Version:** 1.0  
**Date:** 2026-04-13  
**Stack:** Spring Boot 3 · Java 21 · PostgreSQL (Supabase) · JWT  
**Base URL:** `http://localhost:8080`

---

## 1. Scope

| In scope | Out of scope |
|---|---|
| REST API endpoints (all controllers) | Frontend UI |
| Authentication & authorization (JWT, roles) | Google OAuth flow |
| Business logic (transfer, validation, audit) | Infrastructure / DevOps |
| Error handling (4xx, 5xx responses) | Load / performance testing |
| Database constraints (unique, FK) | Email / notification services |

---

## 2. Test Environments

| Environment | URL | Database |
|---|---|---|
| Local | `http://localhost:8080` | `ams_dev` (PostgreSQL local) |
| Staging | `https://api-staging.ams.internal` | `ams_staging` (Supabase) |

**Tools:** Postman · JUnit 5 · Mockito · Spring Boot Test · Testcontainers (PostgreSQL)

---

## 3. Test Accounts (seed data)

| Email | Password | Role |
|---|---|---|
| `alice@company.com` | `123` | admin |
| `bob@company.com` | `123` | asset_manager |
| `dan@company.com` | `123` | department_staff |
| `grace@company.com` | `123` | auditor |
| `iris@company.com` | `123` | *(no role — unassigned)* |

---

## 4. Test Cases

### 4.1 Authentication — `POST /api/auth/login`

| ID | Scenario | Input | Expected |
|---|---|---|---|
| AUTH-01 | Login thành công với admin | `alice@company.com` / `123` | 200 · `accessToken`, `refreshToken`, `user.role = admin` |
| AUTH-02 | Login thành công với asset_manager | `bob@company.com` / `123` | 200 · `user.role = asset_manager` |
| AUTH-03 | Sai password | `alice@company.com` / `wrongpass` | 401 · `"Invalid email or password"` |
| AUTH-04 | Email không tồn tại | `unknown@company.com` / `123` | 401 · `"Invalid email or password"` |
| AUTH-05 | Tài khoản bị khoá (`is_active = false`) | email hợp lệ, account đã lock | 400 · `"Account is deactivated"` |
| AUTH-06 | Thiếu field `email` | body `{ "password": "123" }` | 400 · validation message |
| AUTH-07 | Email sai định dạng | `"email": "notanemail"` | 400 · `"Invalid email format"` |

---

### 4.2 Token — `POST /api/auth/refresh` · `POST /api/auth/logout`

| ID | Scenario | Input | Expected |
|---|---|---|---|
| TOKEN-01 | Không có Authorization header | gọi bất kỳ protected endpoint | 401 · `"Token not found"` |
| TOKEN-02 | Token sai định dạng / random string | `Authorization: Bearer abc123` | 401 · `"Token not found"` |
| TOKEN-03 | Token hết hạn | token expired | 401 · `"Token not found"` |
| TOKEN-04 | Refresh với refresh token hợp lệ | header `X-Refresh-Token: <valid>` | 200 · `accessToken` mới |
| TOKEN-05 | Refresh với refresh token hết hạn | header `X-Refresh-Token: <expired>` | 400 · `"Invalid or expired refresh token"` |
| TOKEN-06 | Logout | `Authorization: Bearer <valid>` | 200 · `"Logged out successfully"` |

---

### 4.3 Users — `GET/PUT /api/users/**`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| USER-01 | Lấy thông tin bản thân (`GET /me`) | any | 200 · trả đúng user đang đăng nhập |
| USER-02 | Lấy danh sách users | admin | 200 · paginated list |
| USER-03 | Lấy danh sách users | asset_manager | 403 · Forbidden |
| USER-04 | Lọc theo `role=auditor` | admin | 200 · chỉ trả auditor |
| USER-05 | Lọc theo `isActive=false` | admin | 200 · chỉ trả user bị lock |
| USER-06 | Lấy user theo ID hợp lệ | admin | 200 · user detail |
| USER-07 | Lấy user theo ID không tồn tại | admin | 404 · `"User not found"` |
| USER-08 | Gán role cho user chưa có role (`iris`) | admin | 200 · role được cập nhật · audit log ghi `role_assigned` |
| USER-09 | Gán role với `role = null` | admin | 400 · validation error |
| USER-10 | Khoá tài khoản (`isActive: false`) | admin | 200 · user bị deactivate |
| USER-11 | Mở khoá tài khoản (`isActive: true`) | admin | 200 · user được activate |
| USER-12 | Thay đổi status của user | asset_manager | 403 · Forbidden |

---

### 4.4 Departments — `GET/POST/PUT /api/departments/**`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| DEPT-01 | Lấy tất cả phòng ban | any | 200 · list sắp xếp theo tên |
| DEPT-02 | Lấy phòng ban theo ID | any | 200 · department detail |
| DEPT-03 | Lấy phòng ban ID không tồn tại | any | 404 |
| DEPT-04 | Tạo phòng ban mới | admin | 201 · department được tạo |
| DEPT-05 | Tạo với code đã tồn tại (`IT`) | admin | 400 · `"Department code already exists"` |
| DEPT-06 | Tạo thiếu `name` | admin | 400 · validation error |
| DEPT-07 | Cập nhật phòng ban | admin | 200 · thông tin được cập nhật |
| DEPT-08 | Tạo phòng ban | asset_manager | 403 · Forbidden |

---

### 4.5 Assets — `GET/POST/PUT /api/assets/**`

#### 4.5.1 Danh sách & Chi tiết

| ID | Scenario | Role | Expected |
|---|---|---|---|
| AST-01 | Lấy danh sách tài sản (không filter) | any | 200 · paginated, default page=0 size=10 |
| AST-02 | Filter theo `status=active` | any | 200 · chỉ active assets |
| AST-03 | Filter theo `category=electronics` | any | 200 · chỉ electronics |
| AST-04 | Filter theo `departmentId` | any | 200 · chỉ assets của phòng ban đó |
| AST-05 | Tìm kiếm theo `search=MacBook` | any | 200 · trả `IT-2024-001` |
| AST-06 | Lấy chi tiết asset theo ID | any | 200 · full detail bao gồm `createdByName` |
| AST-07 | Lấy asset ID không tồn tại | any | 404 · `"Asset not found"` |

#### 4.5.2 Tạo & Cập nhật

| ID | Scenario | Role | Expected |
|---|---|---|---|
| AST-08 | Tạo asset với đầy đủ thông tin | asset_manager | 201 · trả UUID · `asset_code` tự động sinh · audit log `asset_created` |
| AST-09 | Tạo asset không có `departmentId` | asset_manager | 201 · `currentDepartment = null` |
| AST-10 | Tạo thiếu `name` | asset_manager | 400 · `"Asset name is required"` |
| AST-11 | Tạo thiếu `category` | asset_manager | 400 · validation error |
| AST-12 | Cập nhật asset hợp lệ | asset_manager | 200 · thông tin được lưu · audit log `asset_updated` |
| AST-13 | Cập nhật asset với `departmentId` không tồn tại | asset_manager | 404 |
| AST-14 | Tạo / cập nhật asset | department_staff | 403 · Forbidden |

#### 4.5.3 Archive

| ID | Scenario | Role | Expected |
|---|---|---|---|
| AST-15 | Archive asset đang active | asset_manager | 200 · `status = archived` · `archived_at` được set · audit log `asset_archived` |
| AST-16 | Archive asset đã archived | asset_manager | 400 · `"Asset is already archived"` |
| AST-17 | Archive asset | auditor | 403 · Forbidden |

---

### 4.6 Asset Assignments — Transfer & Return

| ID | Scenario | Role | Expected |
|---|---|---|---|
| ASGN-01 | Chuyển asset sang phòng ban mới | asset_manager | 200 · `currentDepartment` cập nhật · assignment cũ có `returned_at` · assignment mới tạo · audit log `asset_transferred` |
| ASGN-02 | Chuyển asset đến `departmentId` không tồn tại | asset_manager | 404 |
| ASGN-03 | Chuyển asset không có `newDepartmentId` | asset_manager | 400 · validation error |
| ASGN-04 | Return asset (đóng assignment) | asset_manager | 200 · open assignment có `returned_at` |
| ASGN-05 | Return asset không có open assignment | asset_manager | 400 · `"No active assignment found"` |
| ASGN-06 | Lấy lịch sử luân chuyển của asset | any | 200 · list sắp xếp mới nhất trước |
| ASGN-07 | Lấy history của asset không tồn tại | any | 404 |

---

### 4.7 Validation Sessions — `GET/POST/PUT /api/validation/sessions/**`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| VAL-01 | Lấy tất cả sessions | admin / auditor | 200 · list sắp xếp năm mới nhất trước |
| VAL-02 | Lấy session theo ID | admin / auditor | 200 · bao gồm `validCount`, `missingCount`, ... |
| VAL-03 | Mở session cho năm mới | admin / auditor | 201 · `status = in_progress` · tự động seed records cho tất cả active assets · audit log `validation_initiated` |
| VAL-04 | Mở session cho năm đã tồn tại | admin / auditor | 400 · `"A validation session for year X already exists"` |
| VAL-05 | Mở session khi đã có session in_progress | admin / auditor | 400 · `"Another session is already in progress"` |
| VAL-06 | Đóng session | admin / auditor | 200 · `status = closed` · `closed_at` được set |
| VAL-07 | Đóng session đã closed | admin / auditor | 400 · `"Session is already closed"` |
| VAL-08 | Lấy records của session (không filter) | any | 200 · tất cả records |
| VAL-09 | Lấy records filter `status=missing` | any | 200 · chỉ missing records |
| VAL-10 | Lấy records filter `departmentId` | any | 200 · chỉ records của phòng ban |
| VAL-11 | Mở session | department_staff | 403 · Forbidden |

---

### 4.8 Validation Status — `PUT /api/validation/assets/{assetId}/status`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| VS-01 | Submit `valid` với note | department_staff | 200 · record cập nhật `status`, `validated_by`, `validated_at` · audit log `validation_record_updated` |
| VS-02 | Submit `missing` | department_staff | 200 · status = missing |
| VS-03 | Submit `invalid` | department_staff | 200 · status = invalid |
| VS-04 | Submit cho asset không có record trong session active | department_staff | 400 · `"No active validation session or no record for this asset"` |
| VS-05 | Submit thiếu `status` | department_staff | 400 · validation error |
| VS-06 | Submit validation | auditor | 403 · Forbidden |

---

### 4.9 Dashboard — `GET /api/dashboard/**`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| DASH-01 | Lấy stats tổng quan | any | 200 · `totalAssets`, `activeAssets`, `archivedAssets`, validation counts đúng |
| DASH-02 | Lấy stats theo phòng ban | any | 200 · list `departmentName`, `assetCount`, `totalValue` |
| DASH-03 | Lấy validation progress khi có session in_progress | any | 200 · trả session đang chạy |
| DASH-04 | Lấy validation progress khi không có session active | any | 200 · `data: null` |

---

### 4.10 Audit Logs — `GET /api/audit-logs/**`

| ID | Scenario | Role | Expected |
|---|---|---|---|
| AUDIT-01 | Lấy tất cả audit logs (phân trang) | admin / auditor | 200 · sắp xếp `created_at DESC` |
| AUDIT-02 | Filter theo `action=asset_created` | admin / auditor | 200 · chỉ asset_created entries |
| AUDIT-03 | Filter theo `assetId` | admin / auditor | 200 · chỉ logs của asset đó |
| AUDIT-04 | Filter theo `performedById` | admin / auditor | 200 · chỉ logs của user đó |
| AUDIT-05 | Filter theo khoảng thời gian `from` / `to` | admin / auditor | 200 · chỉ logs trong khoảng |
| AUDIT-06 | Lấy logs theo asset ID | admin / auditor | 200 · tất cả logs của asset |
| AUDIT-07 | Lấy audit logs | asset_manager | 403 · Forbidden |
| AUDIT-08 | Lấy audit logs | department_staff | 403 · Forbidden |

---

## 5. Role Permission Matrix

| Endpoint | admin | asset_manager | department_staff | auditor |
|---|:---:|:---:|:---:|:---:|
| `POST /auth/login` | ✅ | ✅ | ✅ | ✅ |
| `GET /users` | ✅ | ❌ | ❌ | ❌ |
| `PUT /users/{id}/role` | ✅ | ❌ | ❌ | ❌ |
| `GET /departments` | ✅ | ❌ | ❌ | ❌ |
| `POST /departments` | ✅ | ❌ | ❌ | ❌ |
| `GET /assets` | ✅ | ✅ | ✅ | ✅ |
| `POST /assets` | ✅ | ✅ | ❌ | ❌ |
| `PUT /assets/{id}/archive` | ✅ | ✅ | ❌ | ❌ |
| `POST /assets/{id}/transfer` | ✅ | ✅ | ❌ | ❌ |
| `PUT /assets/{id}/validation-status` | ✅ | ❌ | ✅ | ❌ |
| `POST /validation/sessions` | ✅ | ❌ | ❌ | ✅ |
| `PUT /validation/sessions/{id}/close` | ✅ | ❌ | ❌ | ✅ |
| `GET /dashboard/**` | ✅ | ✅ | ✅ | ✅ |
| `GET /audit-logs` | ✅ | ❌ | ❌ | ✅ |

---

## 6. Error Response Format

Mọi lỗi đều trả về cùng cấu trúc `ApiResponse`:

```json
{
  "status": 401,
  "message": "Token not found",
  "data": null
}
```

| HTTP Status | Khi nào |
|---|---|
| 400 | Validation lỗi, business rule vi phạm |
| 401 | Không có token, token hết hạn, sai password |
| 403 | Có token hợp lệ nhưng không đủ quyền |
| 404 | Resource không tìm thấy |
| 500 | Lỗi server không mong đợi |

---

## 7. Audit Log Coverage

Các hành động sau phải tự động ghi vào `audit_logs` sau khi thực hiện thành công:

| Hành động | Trigger |
|---|---|
| `user_created` | Admin tạo / kích hoạt user |
| `user_deactivated` | Admin lock tài khoản |
| `role_assigned` | Admin gán role cho user |
| `asset_created` | Tạo asset mới |
| `asset_updated` | Cập nhật thông tin asset |
| `asset_archived` | Archive asset |
| `asset_transferred` | Chuyển asset sang phòng ban khác |
| `validation_initiated` | Mở validation session |
| `validation_record_updated` | Staff submit kết quả kiểm kê |

---

## 8. Checklist trước khi Release

- [ ] Tất cả test cases trong mục 4 đã pass
- [ ] Không có endpoint nào trả HTML thay vì JSON khi lỗi
- [ ] 401 trả về đúng cho cả 3 trường hợp: không có token, token sai, token hết hạn
- [ ] Audit log được ghi cho tất cả hành động trong mục 7
- [ ] Seed data từ schema SQL khớp với test accounts ở mục 3
- [ ] `application-prod.yml` không hardcode credentials
- [ ] Swagger UI accessible tại `/swagger-ui/index.html`