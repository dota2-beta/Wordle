package abs.wordle.userservice;

import abs.wordle.userservice.dto.AuthenticationRequestDTO;
import abs.wordle.userservice.dto.AuthenticationResponseDTO;
import abs.wordle.userservice.dto.UserRequestDTO;
import abs.wordle.userservice.enums.UserRole;
import abs.wordle.userservice.exceptions.UserAlreadyExistException;
import abs.wordle.userservice.models.User;
import abs.wordle.userservice.repository.UserRepository;
import abs.wordle.userservice.security.JwtService;
import abs.wordle.userservice.services.AuthenticationService;
import abs.wordle.userservice.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_shouldReturnCorrectUser_whenCredentialsAreValid() {
        // arrange
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("username");
        userRequestDTO.setPassword("rawPassword");
        userRequestDTO.setFirstName("firstName");
        userRequestDTO.setLastName("lastName");

        when(userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("dummy-jwt-token");
        //when(userRepository.save(any(User.class))).thenReturn(user);

        //act
        authenticationService.register(userRequestDTO);

        //assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, Mockito.times(1)).save(captor.capture());

        User user = captor.getValue();
        assertNotNull(user);
        assertEquals("username", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("firstName", user.getFirstName());
        assertEquals(UserRole.USER, user.getRole());
        assertEquals(0, user.getWins());
        assertEquals(0, user.getLosses());
    }

    @Test
    void register_shouldThrowUserAlreadyExistException_whenUsernameAlreadyExists() {
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("existingUser");

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(new User()));
        //act && assert
        assertThrows(UserAlreadyExistException.class, () ->
                authenticationService.register(userRequestDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_shouldReturnToken_whenCredentialsAreValid() {
        //arrange
        AuthenticationRequestDTO authenticationRequestDTO = new AuthenticationRequestDTO();
        authenticationRequestDTO.setUsername("username");
        authenticationRequestDTO.setPassword("rawPassword");

        User user = new User();
        user.setUsername("username");
        user.setPassword("rawPassword");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setId(4L);

        when(userRepository.findByUsername(authenticationRequestDTO.getUsername())).thenReturn(Optional.of(user));
        String expectedToken = "a-very-valid-jwt-token";
        when(jwtService.generateToken(user)).thenReturn(expectedToken);

        //act
        AuthenticationResponseDTO dto = authenticationService.authenticate(authenticationRequestDTO);

        //assert
        assertNotNull(dto);
        assertEquals(expectedToken, dto.getToken());

        verify(userRepository, Mockito.times(1)).findByUsername(authenticationRequestDTO.getUsername());
        verify(authenticationManager, Mockito.times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, Mockito.times(1)).generateToken(user);
    }
}
