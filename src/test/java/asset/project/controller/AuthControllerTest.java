package asset.project.controller;

import asset.project.dto.request.LoginReq;
import asset.project.dto.response.ApiResponse;
import asset.project.dto.response.LoginRes;
import asset.project.enums.UserRole;
import asset.project.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    //should return success
    @Test
    void login_success() {
        LoginReq req = new LoginReq("test@example.com", "password");
        LoginRes res = new LoginRes("access", "refresh",
                new LoginRes.UserInfo(UUID.randomUUID(), "test@example.com", "Test User", UserRole.admin));

        when(authService.login(req)).thenReturn(res);

        ApiResponse<LoginRes> response = authController.login(req);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        assertEquals(res, response.getData());
        verify(authService).login(req);
    }

    //should return success
    @Test
    void refresh_success() {
        String refreshToken = "old-refresh-token";
        LoginRes res = new LoginRes("new-access", "new-refresh",
                new LoginRes.UserInfo(UUID.randomUUID(), "test@example.com", "Test User", UserRole.admin));

        when(authService.refreshToken(refreshToken)).thenReturn(res);

        ApiResponse<LoginRes> response = authController.refresh(refreshToken);

        assertNotNull(response);
        assertEquals(res, response.getData());
        verify(authService).refreshToken(refreshToken);
    }

    // should return OK
    @Test
    void logout_success() {
        String authHeader = "Bearer some-token";
        String token = "some-token";

        ApiResponse<Void> response = authController.logout(authHeader);

        assertNotNull(response);
        assertEquals("Logged out successfully", response.getMessage());
        verify(authService).logout(token);
    }
}
