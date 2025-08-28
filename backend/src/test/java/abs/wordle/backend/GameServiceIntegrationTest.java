package abs.wordle.backend;

import abs.wordle.backend.dto.GuessRequestDTO;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.enums.UserRole;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.models.User;
import abs.wordle.backend.repository.GameRepository;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.security.JwtService;
import abs.wordle.backend.services.GameService;
import abs.wordle.backend.services.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GameServiceIntegrationTest {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void guess_shouldUpdateGameStateAndReturnCorrectLetterStatuses_whenGuessIsValid() throws Exception {
        // arrange
        String word = "PLANE";
        Game game = new Game(word);
        gameRepository.save(game);

        String guess = "GRAVE";
        GuessRequestDTO guessRequestDTO = new GuessRequestDTO();
        guessRequestDTO.setGameId(game.getId());
        guessRequestDTO.setGuess(guess);
        //act
        mockMvc.perform(post("/api/games/guess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guessRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameStatus", is(GameStatus.PROCEED.name())))
                .andExpect(jsonPath("$.currentTry", is(1)))
                .andExpect(jsonPath("$.letterStatuses", contains(
                        LetterStatus.INCORRECT.name(),
                        LetterStatus.INCORRECT.name(),
                        LetterStatus.CORRECT.name(),
                        LetterStatus.INCORRECT.name(),
                        LetterStatus.CORRECT.name()
                )));
    }

    @Test
    void guess_shouldReturnForbidden_whenUserTriesToGuessInAnotherUsersGame() throws Exception {
        //arrange
        String password1 = "qw";
        User user1 = User.builder()
                .username("q")
                .password(passwordEncoder.encode(password1))
                .firstName("qwe")
                .lastName("qwer")
                .role(UserRole.USER)
                .wins(0)
                .losses(0)
                .build();
        userRepository.save(user1);

        String password2 = "as";
        User user2 = User.builder()
                .username("a")
                .password(passwordEncoder.encode(password2))
                .firstName("asd")
                .lastName("asdf")
                .role(UserRole.USER)
                .wins(0)
                .losses(0)
                .build();
        userRepository.save(user2);

        Game game = new Game("WORDLE");
        game.setUser(user1);
        Game savedGame = gameRepository.save(game);

        GuessRequestDTO guessRequest = new GuessRequestDTO();
        guessRequest.setGameId(savedGame.getId());
        guessRequest.setGuess("TESTS");

        String tokenForUser2 = jwtService.generateToken(user2);
        //act && assert
        mockMvc.perform(post("/api/games/guess")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForUser2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(guessRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void guess_shouldReturnBadRequest_whenGameIsAlreadyFinished() throws Exception {
        //arrange
        String word = "PLANE";
        Game game = new Game(word);
        game.setGameStatus(GameStatus.WIN);
        gameRepository.save(game);

        String guess = "GRAVE";
        GuessRequestDTO guessRequestDTO = new GuessRequestDTO();
        guessRequestDTO.setGameId(game.getId());
        guessRequestDTO.setGuess(guess);

        //act && assert
        mockMvc.perform(post("/api/games/guess")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(guessRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guess_shouldReturnBadRequest_whenGuessWordDoesNotExistInDictionary() throws Exception {
        //arrange
        String word = "PLANE";
        Game game = new Game(word);
        gameRepository.save(game);

        String guess = "QQQQQ";
        GuessRequestDTO guessRequestDTO = new GuessRequestDTO();
        guessRequestDTO.setGameId(game.getId());
        guessRequestDTO.setGuess(guess);

        //act && assert
        mockMvc.perform(post("/api/games/guess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guessRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processGuess_shouldSetStatusToWin_whenCorrectGuessIsMadeOnLastTry() throws Exception {
        String password = "as";
        User user = User.builder()
                .username("a")
                .password(passwordEncoder.encode(password))
                .firstName("asd")
                .lastName("asdf")
                .role(UserRole.USER)
                .wins(0)
                .losses(0)
                .build();
        userRepository.save(user);

        Game game = new Game("GRAVE");
        game.setUser(user);
        game.setCurrentTry(5);
        Game savedGame = gameRepository.save(game);

        String tokenForUser = jwtService.generateToken(user);

        String guess = "GRAVE";
        GuessRequestDTO guessRequestDTO = new GuessRequestDTO();
        guessRequestDTO.setGameId(savedGame.getId());
        guessRequestDTO.setGuess(guess);
        //act && assert
        mockMvc.perform(post("/api/games/guess")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenForUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guessRequestDTO)))
                .andExpect(jsonPath("$.gameStatus", is(GameStatus.WIN.name())));

        Game gameFromDb = gameRepository.findById(savedGame.getId()).orElse(null);

        assertNotNull(gameFromDb, "Игра должна существовать в бд");
        assertEquals(GameStatus.WIN, gameFromDb.getGameStatus(), "Статус игры должен быть WIN");

        User userFromDb = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(userFromDb, "Пользователь должен существовать в бд");
        assertEquals(1, userFromDb.getWins(), "Счетчик должен увеличиться на 1");
    }
}
