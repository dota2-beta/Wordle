package abs.wordle.userservice;

import abs.wordle.userservice.enums.UserRole;
import abs.wordle.userservice.models.User;
import abs.wordle.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceUnitTest {
    private final String TEST_SECRET_KEY = "yW5E8u7miY6U1QEOqhOjtTw3WDrWoK0V9e6jNOCfpwYir9rmdZH1Vx7aGap7sDb3IsP83mGqgjdNhtdhypRQGmuAlJRDJbPM4VU6CkTsEuegjHY90SxOEjBKvhmtgtdtsyYMXhTX9dulo78xeZwdR9vjGLewBntDwBtthSCmzRFXPrtA2PpW3kLKE3kXvNZGAJKd6Ug9nKpnxzzfh3i2B9ZDFICIeSPHFk1hILVGbGhXGpT0Lkyq7Liw3rAYPyTr";

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp()
    {
        jwtService = new JwtService(TEST_SECRET_KEY);
        testUser = new User();
        testUser.setId(54L);
        testUser.setUsername("TestUser");
        testUser.setRole(UserRole.USER);
    }

    @Test
    void generateToken_shouldReturnCorrectToken()
    {
        // act
        String token = jwtService.generateToken(testUser);
        // Assert
        assertNotNull(token);
        assertEquals(2, token.chars().filter(c -> c == '.').count());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername_fromGeneratedToken() {
        // arrange
        String token = jwtService.generateToken(testUser);
        // act
        String username = jwtService.extractUsername(token);
        // assert
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void extractUserId_shouldReturnCorrectUserId_fromGeneratedToken() {
        // arrange
        String token = jwtService.generateToken(testUser);
        //act
        Long userId = jwtService.extractUserId(token);
        //assert
        assertNotNull(userId);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forFreshToken() {
        // arrange
        String token = jwtService.generateToken(testUser);
        // act && assert
        assertTrue(jwtService.isTokenValid(token, testUser));
    }
}
