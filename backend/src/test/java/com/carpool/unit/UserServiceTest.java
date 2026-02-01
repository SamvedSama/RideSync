package com.carpool.unit;

import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import com.carpool.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void testUserRegistration() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        Mockito.when(repo.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        Mockito.when(repo.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));

        UserServiceImpl service = new UserServiceImpl(repo);
        User user = new User("Test", "test@mail.com", "pass", "99999", UserRole.RIDER);

        User saved = service.register(user);
        assertEquals("Test", saved.getName());
    }
}
