package com.authored.blogapp;

import com.authored.blogapp.dto.RegisterRequest;
import com.authored.blogapp.model.User;
import com.authored.blogapp.repository.UserRepository;
import com.authored.blogapp.service.UserService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
}
