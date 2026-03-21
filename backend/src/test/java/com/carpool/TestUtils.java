package com.carpool;

import com.carpool.dto.AuthResponse;
import com.carpool.dto.LoginRequest;
import com.carpool.dto.RegisterRequest;
import com.carpool.model.UserRole;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

/**
 * Shared test utilities: register a user and obtain a JWT token
 * so integration tests can make authenticated requests.
 */
public class TestUtils {

    /**
     * Register a user via the API and return the AuthResponse (which includes the
     * JWT token).
     * Throws IllegalStateException if registration fails — makes test failures loud
     * and clear.
     */
    public static AuthResponse register(TestRestTemplate restTemplate,
            String name, String email,
            String password, String phone,
            UserRole role) {
        RegisterRequest req = new RegisterRequest(name, email, password, phone, role);
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/users/register", req, AuthResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException(
                    "Register failed for " + email + " — status: " + resp.getStatusCode() +
                            " body: " + resp.getBody());
        }
        if (resp.getBody().getToken() == null || resp.getBody().getToken().isBlank()) {
            throw new IllegalStateException(
                    "Register returned no token for " + email);
        }
        System.out.println("[TestUtils] Registered: " + email
                + " | token_start=" + resp.getBody().getToken().substring(0, 15));
        return resp.getBody();
    }

    /**
     * Login and return the AuthResponse containing the JWT token.
     */
    public static AuthResponse login(TestRestTemplate restTemplate, String email, String password) {
        LoginRequest req = new LoginRequest(email, password);
        ResponseEntity<AuthResponse> resp = restTemplate.postForEntity(
                "/api/users/login", req, AuthResponse.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new IllegalStateException(
                    "Login failed for " + email + " — status: " + resp.getStatusCode());
        }
        System.out.println("[TestUtils] Registered: " + email
                + " | token_start=" + resp.getBody().getToken().substring(0, 15));
        return resp.getBody();
    }

    /**
     * Build HttpHeaders with Bearer token for authenticated requests.
     */
    public static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Build an HttpEntity with only auth headers and no body.
     */
    public static HttpEntity<Void> authEntity(String token) {
        return new HttpEntity<>(authHeaders(token));
    }

    /**
     * Build an HttpEntity with a body and auth headers.
     */
    public static <T> HttpEntity<T> authEntity(T body, String token) {
        return new HttpEntity<>(body, authHeaders(token));
    }
}
