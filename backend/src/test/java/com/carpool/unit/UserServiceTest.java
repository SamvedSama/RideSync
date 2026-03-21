package com.carpool.unit;

import com.carpool.dto.AuthResponse;
import com.carpool.model.User;
import com.carpool.model.UserRole;
import com.carpool.repository.UserRepository;
import com.carpool.security.JwtTokenProvider;
import com.carpool.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repo;
    private PasswordEncoder encoder;
    private JwtTokenProvider jwtProvider;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        repo = Mockito.mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder();

        // Use real JwtTokenProvider — inject secret via reflection
        jwtProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtProvider, "jwtSecret",
                "TestSecretKeyForJWTSigningThatIsAtLeast256BitsLongForTesting1234567890");
        ReflectionTestUtils.setField(jwtProvider, "jwtExpirationMs", 86400000L);

        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getUserId() == null)
                u.setUserId(1L);
            return u;
        });

        service = new UserServiceImpl(repo, encoder, jwtProvider);
    }

    @Test
    void testRegister_passwordIsHashed() {
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        User user = new User("Test", "test@mail.com", "plainPass", "99999", UserRole.RIDER);
        service.register(user);
        assertNotEquals("plainPass", user.getPassword());
        assertTrue(encoder.matches("plainPass", user.getPassword()));
    }

    @Test
    void testRegister_returnsTokenAndUserData() {
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.empty());
        User user = new User("Test", "test@mail.com", "plainPass", "99999", UserRole.RIDER);
        AuthResponse result = service.register(user);
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isBlank());
        assertEquals("Test", result.getName());
        assertEquals(UserRole.RIDER, result.getRole());
        assertFalse(result.isBanned());
    }

    @Test
    void testRegister_duplicateEmail_throwsException() {
        User existing = new User("Existing", "dup@mail.com", "hashed", "000", UserRole.RIDER);
        when(repo.findByEmail("dup@mail.com")).thenReturn(Optional.of(existing));
        User newUser = new User("New", "dup@mail.com", "pass", "111", UserRole.RIDER);
        assertThrows(IllegalArgumentException.class, () -> service.register(newUser));
        verify(repo, never()).save(any());
    }

    @Test
    void testRegister_driverRole_returnsDriverRoleInResponse() {
        when(repo.findByEmail("driver@mail.com")).thenReturn(Optional.empty());
        User user = new User("Driver", "driver@mail.com", "pass", "123", UserRole.DRIVER);
        AuthResponse result = service.register(user);
        assertEquals(UserRole.DRIVER, result.getRole());
    }

    @Test
    void testLogin_correctCredentials_returnsToken() {
        String hashed = encoder.encode("correctPass");
        User existing = new User("Test", "test@mail.com", hashed, "999", UserRole.RIDER);
        existing.setUserId(5L);
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(existing));
        AuthResponse result = service.login("test@mail.com", "correctPass");
        assertNotNull(result.getToken());
        assertEquals("Test", result.getName());
    }

    @Test
    void testLogin_wrongPassword_throwsException() {
        String hashed = encoder.encode("correctPass");
        User existing = new User("Test", "test@mail.com", hashed, "999", UserRole.RIDER);
        when(repo.findByEmail("test@mail.com")).thenReturn(Optional.of(existing));
        assertThrows(IllegalArgumentException.class,
                () -> service.login("test@mail.com", "wrongPass"));
    }

    @Test
    void testLogin_unknownEmail_throwsException() {
        when(repo.findByEmail("nobody@mail.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.login("nobody@mail.com", "pass"));
    }

    @Test
    void testLogin_bannedUser_throwsExceptionContainingBan() {
        String hashed = encoder.encode("pass");
        User banned = new User("Banned", "banned@mail.com", hashed, "000", UserRole.RIDER);
        banned.setBanned(true);
        when(repo.findByEmail("banned@mail.com")).thenReturn(Optional.of(banned));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.login("banned@mail.com", "pass"));
        assertTrue(ex.getMessage().toLowerCase().contains("ban"));
    }

    @Test
    void testUpdateProfile_updatesNameAndPhone() {
        User existing = new User("Old Name", "u@mail.com", "hash", "000", UserRole.RIDER);
        existing.setUserId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User result = service.updateProfile(10L, "New Name", "1234567890");
        assertEquals("New Name", result.getName());
        assertEquals("1234567890", result.getPhone());
        verify(repo, times(1)).save(existing);
    }

    @Test
    void testUpdateProfile_userNotFound_throwsException() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.updateProfile(99L, "Name", "Phone"));
    }

    @Test
    void testGetProfile_existingUser_returnsUser() {
        User user = new User("Alice", "alice@mail.com", "hash", "111", UserRole.RIDER);
        user.setUserId(7L);
        when(repo.findById(7L)).thenReturn(Optional.of(user));
        User result = service.getProfile(7L);
        assertEquals("Alice", result.getName());
    }

    @Test
    void testGetProfile_notFound_throwsException() {
        when(repo.findById(88L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getProfile(88L));
    }
}