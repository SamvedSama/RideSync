package com.carpool.integration;

import com.carpool.TestUtils;
import com.carpool.dto.AuthResponse;
import com.carpool.dto.LoginRequest;
import com.carpool.dto.RegisterRequest;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    // ── Register ──────────────────────────────────────────────────────────────

    @Test
    void testRegister_returnsTokenAndUserData() {
        RegisterRequest req = new RegisterRequest("Alice", "alice@test.com", "password123", "9999999999", UserRole.RIDER);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/api/users/register", req, AuthResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        AuthResponse body = response.getBody();
        assertNotNull(body.getToken(), "JWT token must be returned");
        assertEquals("Alice", body.getName());
        assertEquals("alice@test.com", body.getEmail());
        assertEquals(UserRole.RIDER, body.getRole());
        assertFalse(body.isBanned());

        // Confirm user persisted
        assertTrue(userRepository.findByEmail("alice@test.com").isPresent());
    }

    @Test
    void testRegister_duplicateEmail_returns400() {
        RegisterRequest req = new RegisterRequest("Alice", "dup@test.com", "password123", "9999", UserRole.RIDER);
        restTemplate.postForEntity("/api/users/register", req, AuthResponse.class);

        ResponseEntity<String> second = restTemplate.postForEntity("/api/users/register", req, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, second.getStatusCode());
    }

    @Test
    void testRegister_missingName_returns400() {
        RegisterRequest req = new RegisterRequest("", "noname@test.com", "password123", "9999", UserRole.RIDER);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/users/register", req, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Test
    void testLogin_validCredentials_returnsToken() {
        TestUtils.register(restTemplate, "Bob", "bob@test.com", "securePass", "1234567890", UserRole.DRIVER);

        LoginRequest loginReq = new LoginRequest("bob@test.com", "securePass");
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity("/api/users/login", loginReq, AuthResponse.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody().getToken());
        assertEquals("Bob", resp.getBody().getName());
        assertEquals(UserRole.DRIVER, resp.getBody().getRole());
    }

    @Test
    void testLogin_wrongPassword_returns400() {
        TestUtils.register(restTemplate, "Carol", "carol@test.com", "rightPass", "111", UserRole.RIDER);

        LoginRequest bad = new LoginRequest("carol@test.com", "wrongPass");
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/users/login", bad, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void testLogin_unknownEmail_returns400() {
        LoginRequest bad = new LoginRequest("nobody@test.com", "pass");
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/users/login", bad, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    // ── Authenticated endpoints ───────────────────────────────────────────────

    @Test
    void testGetMe_authenticated_returnsCurrentUser() {
        AuthResponse auth = TestUtils.register(restTemplate, "Dave", "dave@test.com", "pass123", "555", UserRole.RIDER);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/users/me", HttpMethod.GET,
                TestUtils.authEntity(auth.getToken()), String.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("dave@test.com"));
    }

    @Test
    void testGetMe_unauthenticated_returns401() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/users/me", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
    }

    @Test
    void testUpdateProfile_authenticated_succeeds() {
        AuthResponse auth = TestUtils.register(restTemplate, "Eve", "eve@test.com", "pass123", "777", UserRole.RIDER);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/users/" + auth.getUserId() + "?name=EveUpdated&phone=8888888888",
                HttpMethod.PUT, TestUtils.authEntity(auth.getToken()), String.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody().contains("EveUpdated"));
    }
}
