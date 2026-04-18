package asset.project.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private final String secret = "9a4f2c8d3b7a1e6f4g8h2i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d";

    @BeforeEach
    void setUp() {
        // 7 days
        long refreshExp = 604800000;
        // 1 hour
        long accessExp = 3600000;
        jwtUtils = new JwtUtils(secret, accessExp, refreshExp);
    }

    @Test
    void generateAccessToken_ShouldCreateValidToken() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "admin";

        String token = jwtUtils.generateAccessToken(userId, email, role);

        assertNotNull(token);
        assertTrue(jwtUtils.isTokenValid(token));
        
        Claims claims = jwtUtils.parseToken(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals(email, claims.get("email"));
        assertEquals(role, claims.get("role"));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() throws InterruptedException {
        // Create a JwtUtils with very short expiration
        JwtUtils shortLivedJwtUtils = new JwtUtils(secret, 1, 1);
        UUID userId = UUID.randomUUID();
        
        String token = shortLivedJwtUtils.generateAccessToken(userId, "test@example.com", "admin");
        
        // Wait for token to expire
        Thread.sleep(10);
        
        assertFalse(shortLivedJwtUtils.isTokenValid(token));
        
        // Directly test that parseToken throws ExpiredJwtException or JwtException
        assertThrows(Exception.class, () -> shortLivedJwtUtils.parseToken(token));
    }

    @Test
    void extractUserId_ShouldReturnCorrectId() {
        UUID userId = UUID.randomUUID();
        String token = jwtUtils.generateRefreshToken(userId);
        
        UUID extractedId = jwtUtils.extractUserId(token);
        
        assertEquals(userId, extractedId);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForInvalidToken() {
        assertFalse(jwtUtils.isTokenValid("invalid.token.here"));
    }
}
