package abs.wordle.userservice;

import abs.wordle.userservice.dto.AuthenticationRequestDTO;
import abs.wordle.userservice.dto.UserRequestDTO;
import abs.wordle.userservice.enums.UserRole;
import abs.wordle.userservice.models.User;
import abs.wordle.userservice.repository.UserRepository;
import abs.wordle.userservice.security.JwtService;
import abs.wordle.userservice.services.AuthenticationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserServiceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthenticationService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Test
    void register_shouldCreateUserAndReturnToken_whenRequestIsValid() throws Exception {
        //arrange
        UserRequestDTO userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("username");
        userRequestDTO.setPassword("password");
        userRequestDTO.setFirstName("firstName");
        userRequestDTO.setLastName("lastName");
        //act
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
        //assert
        assertTrue(userRepository.findByUsername("username").isPresent(),
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

    @Test
    void save_shouldStorePasswordInHashedFormat() throws Exception {
        // arrange
        String rawPassword = "password";
        var userToSave = User.builder()
                .firstName("firstname")
                .lastName("lastname")
                .username("username")
                .password(passwordEncoder.encode(rawPassword))
                .wins(0)
                .losses(0)
                .createdAt(LocalDateTime.now())
                .role(UserRole.USER)
                .build();

        //act
        User savedUser = userRepository.save(userToSave);

        //assert
        assertNotEquals(rawPassword, savedUser.getPassword());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    void getUserProfile_shouldReturnCorrectUser_whenRequestIsValid() throws Exception {
        //arrange
        var user = User.builder()
                .firstName("firstname")
                .lastName("lastname")
                .username("username")
                .password(passwordEncoder.encode("password"))
                .wins(0)
                .losses(0)
                .createdAt(LocalDateTime.now())
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        //act && assert
        mockMvc.perform(get("/api/users/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("X-User-Id", user.getId().toString())
                    .header("X-Username", user.getUsername())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user.getLastName()))
                .andExpect(jsonPath("$.role").value(user.getRole().name()))
                .andExpect(jsonPath("$.wins").value(user.getWins()))
                .andExpect(jsonPath("$.losses").value(user.getLosses()));
    }

    @Test
    void getTop20Users_shouldReturnCorrectUsers_whenRequestIsValid() throws Exception {
        //arrange
        List<User> users = new ArrayList<>();
        for(int i = 0; i < 25; i++) {
            users.add(User.builder()
                    .username("username" + i)
                    .password("password" + i)
                    .firstName("firstname" + i)
                    .lastName("lastname" + i)
                    .role(UserRole.USER)
                    .wins(i)
                    .build()
            );
        }
        userRepository.saveAll(users);
        //act
        MvcResult result = mockMvc.perform(get("/api/users/top")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        //assert
        String resultJson = result.getResponse().getContentAsString();
        LinkedHashMap<String, Integer> top20Users = objectMapper.readValue(resultJson, new TypeReference<>() {});

        List<Integer> wins = new ArrayList<>(top20Users.values());
        for(int i = 0; i <= wins.size() - 2; i++) {
            assertTrue(wins.get(i) >= wins.get(i + 1));
        }
        assertEquals(12, top20Users.get("username12"));
        assertEquals("username24", top20Users.keySet().iterator().next());
        assertEquals(20, top20Users.size());
    }

    @Test
    void getUserForInternalCommunication_shouldReturnCorrectUser_whenRequestIsValid() throws Exception {
        //arrange
        User user = User.builder()
                .firstName("firstname")
                .lastName("lastname")
                .username("username")
                .password("password")
                .wins(5)
                .build();
        User savedUser = userRepository.save(user);

        //act && assert
        mockMvc.perform(get("/api/users/internal/" + savedUser.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value(savedUser.getUsername()));

    }

    @Test
    void getMyRank_shouldReturnCorrectRank_whenUserIsAuthenticated() throws Exception {
        List<User> users = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
            users.add(User.builder()
                    .username("username" + i)
                    .firstName("firstname" + i)
                    .lastName("lastname" + i)
                    .wins(i)
                    .build()
            );
        }
        userRepository.saveAll(users);
        User testUser = User.builder()
                .username("testUser")
                .firstName("fn")
                .lastName("ln")
                .wins(52)
                .build();
        userRepository.save(testUser);

        String token = jwtService.generateToken(testUser);
        mockMvc.perform(
                    get("/api/users/me/rank")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + token)
                    .header("X-Username", testUser.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isMap())
            .andExpect(jsonPath("$.rank").value(1));

    }
}
