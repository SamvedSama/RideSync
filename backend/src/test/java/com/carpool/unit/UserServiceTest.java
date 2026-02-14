package com.carpool.unit;

import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import com.carpool.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void testSuccessfulRegistration() {

        UserRepository repo = Mockito.mock(UserRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        Mockito.when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.empty());

        Mockito.when(repo.save(Mockito.any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserServiceImpl service = new UserServiceImpl(repo, encoder);

        User user = new User("Test", "test@mail.com",
                "plainPass", "99999", UserRole.RIDER);

        User saved = service.register(user);

        assertNotEquals("plainPass", saved.getPassword());
        assertTrue(encoder.matches("plainPass", saved.getPassword()));
    }

    @Test
    void testLoginFailsWrongPassword() {

        UserRepository repo = Mockito.mock(UserRepository.class);
        PasswordEncoder encoder = new BCryptPasswordEncoder();

        String hashed = encoder.encode("correctPass");

        User existing = new User("Test",
                "test@mail.com",
                hashed,
                "999",
                UserRole.RIDER);

        Mockito.when(repo.findByEmail("test@mail.com"))
                .thenReturn(Optional.of(existing));

        UserServiceImpl service = new UserServiceImpl(repo, encoder);

        assertThrows(IllegalArgumentException.class,
                () -> service.login("test@mail.com", "wrongPass"));
    }
}
