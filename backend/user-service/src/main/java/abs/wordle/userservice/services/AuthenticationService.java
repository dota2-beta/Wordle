package abs.wordle.userservice.services;

import abs.wordle.userservice.dto.AuthenticationRequestDTO;
import abs.wordle.userservice.dto.AuthenticationResponseDTO;
import abs.wordle.userservice.dto.UserRequestDTO;
import abs.wordle.userservice.enums.UserRole;
import abs.wordle.userservice.exceptions.UserAlreadyExistException;
import abs.wordle.userservice.repository.UserRepository;
import abs.wordle.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import abs.wordle.userservice.models.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponseDTO register(UserRequestDTO request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistException("Username already exists");
        }
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .wins(0)
                .losses(0)
                .createdAt(LocalDateTime.now())
                .role(UserRole.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponseDTO authenticate(AuthenticationRequestDTO request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(request.getUsername()));
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}
