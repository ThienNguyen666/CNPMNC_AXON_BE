package asset.project.service.impl;

import asset.project.dto.request.LoginReq;
import asset.project.dto.response.LoginRes;
import asset.project.entity.User;
import asset.project.enums.UserRole;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.UserRepository;
import asset.project.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private UUID userId;
    private User inactivateUser;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .role(UserRole.admin)
                .isActive(true)
                .build();
        userId = UUID.randomUUID();
        inactivateUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .fullName("Test User")
                .role(UserRole.department_staff)
                .isActive(false)
                .build();

    }

    @Test
    void login_ShouldReturnLoginRes_WhenCredentialsAreValid() {
        LoginReq req = new LoginReq("test@example.com", "password123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(req.password(), mockUser.getPasswordHash())).thenReturn(true);
        when(jwtUtils.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("refresh-token");

        LoginRes result = authService.login(req);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(mockUser.getEmail(), result.user().email());
        verify(userRepository).findByEmail(req.email());
        verify(passwordEncoder).matches(req.password(), mockUser.getPasswordHash());
    }

    @Test
    void login_ShouldThrowBadCredentials_WhenUserNotFound() {
        LoginReq req = new LoginReq("notfound@example.com", "password");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void login_ShouldThrowBusinessException_WhenUserIsInactive() {
        mockUser.setActive(false);
        LoginReq req = new LoginReq("test@example.com", "password123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(mockUser));

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(req));
        assertEquals("Account is deactivated", ex.getMessage());
    }

    @Test
    void login_ShouldThrowBadCredentials_WhenPasswordIsIncorrect() {
        LoginReq req = new LoginReq("test@example.com", "wrongpassword");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(req.password(), mockUser.getPasswordHash())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenTokenIsValid() {
        String oldRefreshToken = "valid-refresh-token";
        when(jwtUtils.isTokenValid(oldRefreshToken)).thenReturn(true);
        when(jwtUtils.extractUserId(oldRefreshToken)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(jwtUtils.generateAccessToken(any(), anyString(), anyString())).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(any())).thenReturn("new-refresh-token");

        LoginRes result = authService.refreshToken(oldRefreshToken);

        assertNotNull(result);
        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-refresh-token", result.refreshToken());
    }

    @Test
    void refreshToken_ShouldThrowBusinessException_WhenTokenIsInvalid() {
        String invalidToken = "invalid-token";
        when(jwtUtils.isTokenValid(invalidToken)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(invalidToken));
        assertEquals("Invalid or expired refresh token", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowBusinessException_WhenTokenIsExpired() {
        String expiredToken = "expired-token";
        // In our implementation, isTokenValid returns false for expired tokens
        when(jwtUtils.isTokenValid(expiredToken)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.refreshToken(expiredToken));
        assertEquals("Invalid or expired refresh token", ex.getMessage());
    }

    @Test
    void refreshToken_ShouldThrowResourceNotFound_WhenUserNotFound() {
        String token = "valid-token-no-user";
        when(jwtUtils.isTokenValid(token)).thenReturn(true);
        when(jwtUtils.extractUserId(token)).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refreshToken(token));
    }

    //not through anything
    @Test
    void logout_ShouldDoNothing() {
        assertDoesNotThrow(() -> authService.logout("some-token"));
    }

}
