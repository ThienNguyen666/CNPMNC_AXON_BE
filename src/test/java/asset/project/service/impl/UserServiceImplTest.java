package asset.project.service.impl;

import asset.project.dto.request.AssignRoleReq;
import asset.project.dto.request.UpdateUserStatusReq;
import asset.project.dto.response.UserRes;
import asset.project.entity.User;
import asset.project.enums.UserRole;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.UserRepository;
import asset.project.service.AuditLogService;
import asset.project.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser1;
    private User mockUser2;
    private User mockUser3;
    private User mockUser4;
    private User actor;

    @BeforeEach
    void setUp() {
        mockUser1 = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .fullName("Test User")
                .role(UserRole.department_staff)
                .isActive(true)
                .build();
        mockUser2 = User.builder()
                .id(UUID.randomUUID())
                .email("test@example2.com")
                .fullName("Test Admin")
                .role(UserRole.admin)
                .isActive(true)
                .build();
        mockUser3 = User.builder()
                .id(UUID.randomUUID())
                .email("test@example3.com")
                .fullName("Test Auditor")
                .role(UserRole.auditor)
                .isActive(true)
                .build();
        mockUser4 = User.builder()
                .id(UUID.randomUUID())
                .email("test@example4.com")
                .fullName("Test Manager")
                .role(UserRole.asset_manager)
                .isActive(true)
                .build();

        actor = User.builder().id(UUID.randomUUID()).role(UserRole.admin).build();
    }

    @Test
    void getById_ShouldReturnUser_WhenExists1() {
        when(userRepository.findById(mockUser1.getId())).thenReturn(Optional.of(mockUser1));

        UserRes result = userService.getById(mockUser1.getId());

        assertNotNull(result);
        assertEquals(mockUser1.getEmail(), result.email());
        verify(userRepository).findById(mockUser1.getId());
    }

    @Test
    void getById_ShouldThrowException_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void assignRole_ShouldUpdateRoleAndLog1() {
        AssignRoleReq req = new AssignRoleReq(UserRole.asset_manager);
        when(userRepository.findById(mockUser1.getId())).thenReturn(Optional.of(mockUser1));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.assignRole(mockUser1.getId(), req);

        assertEquals(UserRole.asset_manager, mockUser1.getRole());
        verify(userRepository).save(mockUser1);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser1), any(), any(), any());
    }

    @Test
    void updateStatus_ShouldUpdateStatusAndLog1() {
        UpdateUserStatusReq req = new UpdateUserStatusReq(false);
        when(userRepository.findById(mockUser1.getId())).thenReturn(Optional.of(mockUser1));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.updateStatus(mockUser1.getId(), req);

        assertFalse(mockUser1.isActive());
        verify(userRepository).save(mockUser1);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser1), any(), any(), any());
    }

    @Test
    void getById_ShouldReturnUser_WhenExists2() {
        when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));

        UserRes result = userService.getById(mockUser2.getId());

        assertNotNull(result);
        assertEquals(mockUser2.getEmail(), result.email());
        verify(userRepository).findById(mockUser2.getId());
    }


    @Test
    void assignRole_ShouldUpdateRoleAndLog2() {
        AssignRoleReq req = new AssignRoleReq(UserRole.asset_manager);
        when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.assignRole(mockUser2.getId(), req);

        assertEquals(UserRole.asset_manager, mockUser2.getRole());
        verify(userRepository).save(mockUser2);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser2), any(), any(), any());
    }


    @Test
    void updateStatus_ShouldUpdateStatusAndLog2() {
        UpdateUserStatusReq req = new UpdateUserStatusReq(false);
        when(userRepository.findById(mockUser2.getId())).thenReturn(Optional.of(mockUser2));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.updateStatus(mockUser2.getId(), req);

        assertFalse(mockUser2.isActive());
        verify(userRepository).save(mockUser2);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser2), any(), any(), any());
    }


    @Test
    void getById_ShouldReturnUser_WhenExists3() {
        when(userRepository.findById(mockUser3.getId())).thenReturn(Optional.of(mockUser3));

        UserRes result = userService.getById(mockUser3.getId());

        assertNotNull(result);
        assertEquals(mockUser3.getEmail(), result.email());
        verify(userRepository).findById(mockUser3.getId());
    }


    @Test
    void assignRole_ShouldUpdateRoleAndLog3() {
        AssignRoleReq req = new AssignRoleReq(UserRole.asset_manager);
        when(userRepository.findById(mockUser3.getId())).thenReturn(Optional.of(mockUser3));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.assignRole(mockUser3.getId(), req);

        assertEquals(UserRole.asset_manager, mockUser3.getRole());
        verify(userRepository).save(mockUser3);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser3), any(), any(), any());
    }


    @Test
    void updateStatus_ShouldUpdateStatusAndLog3() {
        UpdateUserStatusReq req = new UpdateUserStatusReq(false);
        when(userRepository.findById(mockUser3.getId())).thenReturn(Optional.of(mockUser3));
        when(securityUtils.getCurrentUser()).thenReturn(actor);

        userService.updateStatus(mockUser3.getId(), req);

        assertFalse(mockUser3.isActive());
        verify(userRepository).save(mockUser3);
        verify(auditLogService).log(any(), eq(actor), any(), eq(mockUser3), any(), any(), any());
    }
}
