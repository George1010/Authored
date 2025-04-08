package com.authored.blogapp;

import com.authored.blogapp.dto.LoginRequest;
import com.authored.blogapp.dto.LoginResponse;
import com.authored.blogapp.dto.RegisterRequest;
import com.authored.blogapp.model.User;
import com.authored.blogapp.repository.UserRepository;
import com.authored.blogapp.service.TokenService;
import com.authored.blogapp.service.UserService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;
    
    @Mock
    private AuthenticationManager authenticationManager;
    
    @InjectMocks
    private UserService userService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldRegisterUserWhenEmailIsNew() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        // When
        userService.registerUser(request);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "Email already exists")
    public void shouldThrowExceptionWhenEmailExists() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("pass");

        User existingUser = new User();
        existingUser.setEmail("jane@example.com");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(existingUser));

        // When
        userService.registerUser(request);

        // Then – Exception is expected
    }

     @Test
    public void shouldLoginSuccessfullyWithValidCredentials() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword"); // mock encoded
        user.setRole("USER");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("mocked-jwt");

        // When
        LoginResponse response = userService.loginUser(request);

        // Then
        assertNotNull(response);
        assertEquals(response.getToken(), "mocked-jwt");
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "Invalid credentials")
    public void shouldThrowExceptionForInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        userService.loginUser(request);
    }
}
