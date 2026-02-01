package com.carpool.integration;

import com.carpool.model.User;
import com.carpool.model.UserRole;
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

    @Test
    void testRegisterEndpoint() {
        User user = new User("Alice", "alice@mail.com", "1234", "8888", UserRole.RIDER);

        ResponseEntity<User> response =
                restTemplate.postForEntity("/api/users/register", user, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
