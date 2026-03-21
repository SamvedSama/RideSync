package com.carpool.unit;

import com.carpool.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider.
 * Tests token generation, validation, and claim extraction.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "TestSecretKeyForJWTSigningThatIsAtLeast256BitsLongForTesting1234567890");
        ReflectionTestUtils.setField(provider, "jwtExpirationMs", 86400000L);
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    void testGenerateToken_returnsNonNullString() {
        String token = provider.generateToken(1L, "user@test.com", "RIDER");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void testGenerateToken_differentUsersProduceDifferentTokens() {
        String t1 = provider.generateToken(1L, "user1@test.com", "RIDER");
        String t2 = provider.generateToken(2L, "user2@test.com", "DRIVER");
        assertNotEquals(t1, t2);
    }

    @Test
    void testGenerateToken_hasThreeParts() {
        String token = provider.generateToken(1L, "user@test.com", "RIDER");
        assertEquals(3, token.split("\\.").length, "JWT must have 3 dot-separated parts");
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    void testValidateToken_validToken_returnsTrue() {
        String token = provider.generateToken(1L, "user@test.com", "RIDER");
        assertTrue(provider.validateToken(token));
    }

    @Test
    void testValidateToken_tamperedToken_returnsFalse() {
        String token = provider.generateToken(1L, "user@test.com", "RIDER");
        String tampered = token.substring(0, token.length() - 4) + "XXXX";
        assertFalse(provider.validateToken(tampered));
    }

    @Test
    void testValidateToken_randomString_returnsFalse() {
        assertFalse(provider.validateToken("not.a.jwt"));
    }

    @Test
    void testValidateToken_emptyString_returnsFalse() {
        assertFalse(provider.validateToken(""));
    }

    @Test
    void testValidateToken_expiredToken_returnsFalse() {
        JwtTokenProvider shortProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortProvider, "jwtSecret",
                "TestSecretKeyForJWTSigningThatIsAtLeast256BitsLongForTesting1234567890");
        ReflectionTestUtils.setField(shortProvider, "jwtExpirationMs", 1L); // 1ms TTL

        String token = shortProvider.generateToken(1L, "user@test.com", "RIDER");

        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        assertFalse(shortProvider.validateToken(token));
    }

    // ── claim extraction ──────────────────────────────────────────────────────

    @Test
    void testGetEmailFromToken_returnsCorrectEmail() {
        String token = provider.generateToken(1L, "extract@test.com", "DRIVER");
        assertEquals("extract@test.com", provider.getEmailFromToken(token));
    }

    @Test
    void testGetUserIdFromToken_returnsCorrectId() {
        String token = provider.generateToken(42L, "user@test.com", "RIDER");
        assertEquals(42L, provider.getUserIdFromToken(token));
    }

    @Test
    void testGetRoleFromToken_returnsCorrectRole() {
        String token = provider.generateToken(1L, "admin@test.com", "ADMIN");
        assertEquals("ADMIN", provider.getRoleFromToken(token));
    }

    @Test
    void testGetRoleFromToken_riderRole() {
        String token = provider.generateToken(5L, "rider@test.com", "RIDER");
        assertEquals("RIDER", provider.getRoleFromToken(token));
    }
}
