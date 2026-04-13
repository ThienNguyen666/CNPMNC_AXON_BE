package asset.project.service.impl;

import asset.project.dto.request.LoginReq;
import asset.project.dto.response.LoginRes;
import asset.project.entity.User;
import asset.project.exception.BusinessException;
import asset.project.exception.ResourceNotFoundException;
import asset.project.repository.UserRepository;
import asset.project.service.AuthService;
import asset.project.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public LoginRes login(LoginReq req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!user.isActive()) {
            throw new BusinessException("Account is deactivated");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String roleStr = user.getRole() != null ? user.getRole().name() : "no_role";
        String access  = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), roleStr);
        String refresh = jwtUtils.generateRefreshToken(user.getId());

        return new LoginRes(access, refresh,
                new LoginRes.UserInfo(user.getId(), user.getEmail(), user.getFullName(), user.getRole()));
    }

    @Override
    public LoginRes refreshToken(String refreshToken) {
        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new BusinessException("Invalid or expired refresh token");
        }
        var userId = jwtUtils.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String roleStr = user.getRole() != null ? user.getRole().name() : "no_role";
        String newAccess  = jwtUtils.generateAccessToken(user.getId(), user.getEmail(), roleStr);
        String newRefresh = jwtUtils.generateRefreshToken(user.getId());

        return new LoginRes(newAccess, newRefresh,
                new LoginRes.UserInfo(user.getId(), user.getEmail(), user.getFullName(), user.getRole()));
    }

    @Override
    public void logout(String token) {
        // For stateless JWT: client discards the token.
        // Extend here with a token blacklist/Redis if needed.
    }
}