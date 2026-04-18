package asset.project.controller;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.PageRes;
import asset.project.dto.response.UserRes;
import asset.project.enums.UserRole;
import asset.project.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private UserRes testUserRes;
    private UserRes testUserRes2;
    private UserRes testUserRes3;
    private UserRes testUserRes4;
    private final UUID userId = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();
    private final UUID userId3 = UUID.randomUUID();
    private final UUID userId4 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        testUserRes = new UserRes(
                userId,
                "test@example.com",
                "Test User",
                UserRole.admin,
                UUID.randomUUID(),
                "IT Department",
                true,
                OffsetDateTime.now()
        );

        // Case other roles
        testUserRes2 = new UserRes(
                userId2,
                "test2@example.com",
                "Test User2",
                UserRole.asset_manager,
                UUID.randomUUID(),
                "IT Department",
                false,
                OffsetDateTime.now()
        );

        testUserRes3 = new UserRes(
                userId3,
                "test3@example.com",
                "Test User3",
                UserRole.department_staff,
                UUID.randomUUID(),
                "IT Department",
                true,
                OffsetDateTime.now()
        );

        testUserRes4 = new UserRes(
                userId4,
                "test4@example.com",
                "Test User4",
                UserRole.auditor,
                UUID.randomUUID(),
                "Financial Department",
                false,
                OffsetDateTime.now()
        );
    }


    @Test
    void getMe() throws Exception {
        when(userService.getMe()).thenReturn(testUserRes);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"))
                .andExpect(jsonPath("$.data.role").value(UserRole.admin.name()));
    }

    @Test
    void getById() throws Exception {
        when(userService.getById(userId)).thenReturn(testUserRes);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"));
    }

    @Test
    void getAll() throws Exception {
        PageRes<UserRes> pageRes = PageRes.<UserRes>builder()
                .content(Collections.singletonList(testUserRes))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        when(userService.getAll(any(), any(), any(), any(Pageable.class))).thenReturn(pageRes);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.content[0].id").value(userId.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getMe2() throws Exception {
        when(userService.getMe()).thenReturn(testUserRes2);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId2.toString()))
                .andExpect(jsonPath("$.data.email").value("test2@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User2"))
                .andExpect(jsonPath("$.data.role").value(UserRole.asset_manager.name()));
    }

    @Test
    void getMe3() throws Exception {
        when(userService.getMe()).thenReturn(testUserRes3);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId3.toString()))
                .andExpect(jsonPath("$.data.email").value("test3@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User3"))
                .andExpect(jsonPath("$.data.role").value(UserRole.department_staff.name()));
    }

    @Test
    void getMe4() throws Exception {
        when(userService.getMe()).thenReturn(testUserRes3);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId3.toString()))
                .andExpect(jsonPath("$.data.email").value("test3@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User3"))
                .andExpect(jsonPath("$.data.role").value(UserRole.department_staff.name()));
    }

    @Test
    void getMe5() throws Exception {
        when(userService.getMe()).thenReturn(testUserRes4);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId4.toString()))
                .andExpect(jsonPath("$.data.email").value("test4@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User4"))
                .andExpect(jsonPath("$.data.role").value(UserRole.auditor.name()));
    }

    @Test
    void getById2() throws Exception {
        when(userService.getById(userId2)).thenReturn(testUserRes2);

        mockMvc.perform(get("/api/users/{id}", userId2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.id").value(userId2.toString()))
                .andExpect(jsonPath("$.data.email").value("test2@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User2"));
    }
}
