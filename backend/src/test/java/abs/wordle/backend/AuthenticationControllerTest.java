package abs.wordle.backend;

import abs.wordle.backend.dto.AuthenticationRequestDTO;
import abs.wordle.backend.dto.UserRequestDTO;
import abs.wordle.backend.enums.UserRole;
import abs.wordle.backend.models.User;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder  passwordEncoder;

    @Test
    void register_shouldCreateUserAndReturnToken_whenRequestIsValid() throws Exception {
        //arrange
        UserRequestDTO userRequestDTO = UserRequestDTO.builder()
                .firstName("qw")
                .lastName("qwe")
                .username("qwerty")
                .password("qweqwe")
                .build();
        //act
        mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        assertTrue(userRepository.findByUsername("qwerty").isPresent(),
                "Пользователь qwerty должен был быть сохранен в бд");
    }

    @Test
    void login_shouldReturnToken_whenRequestIsValid() throws Exception {
        //arrange
        String password = "zx";
        User user = User.builder()
                .username("z")
                .password(passwordEncoder.encode(password))
                .firstName("zxc")
                .lastName("zxc")
                .role(UserRole.USER)
                .wins(0)
                .losses(0)
                .build();
        userRepository.save(user);

        AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                .username("z")
                .password(password)
                .build();

        //act & assert
        mockMvc.perform(post("/api/auth/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authenticationRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}
