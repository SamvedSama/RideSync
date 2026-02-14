package com.carpool.integration;

import com.carpool.dto.RegisterRequest;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegisterEndpoint() {

        RegisterRequest request = new RegisterRequest(
                "Alice",
                "alice@mail.com",
                "1234",
                "8888",
                UserRole.RIDER);

        ResponseEntity<User> response = restTemplate.postForEntity(
                "/api/users/register",
                request,
                User.class);

        // 🔹 Strong assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        User savedUser = response.getBody();

        assertNotNull(savedUser.getUserId());
        assertEquals("Alice", savedUser.getName());
        assertEquals("alice@mail.com", savedUser.getEmail());
        assertEquals(UserRole.RIDER, savedUser.getRole());

        // Ensure password is encoded (not raw "1234")
        assertNotEquals("1234", savedUser.getPassword());

        // Ensure user actually exists in DB
        assertTrue(userRepository.findByEmail("alice@mail.com").isPresent());
    }
}
