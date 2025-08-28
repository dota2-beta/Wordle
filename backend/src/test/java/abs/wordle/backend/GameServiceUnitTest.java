package abs.wordle.backend;

import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.enums.UserRole;
import abs.wordle.backend.models.Attempt;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.models.User;
import abs.wordle.backend.repository.AttemptRepository;
import abs.wordle.backend.repository.GameRepository;
import abs.wordle.backend.repository.LetterStatusesRepository;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.services.GameServiceImpl;
import abs.wordle.backend.utils.WordleUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private LetterStatusesRepository letterStatusesRepository;
    @Mock
    private AttemptRepository attemptRepository;
    @InjectMocks
    private GameServiceImpl gameService;

    private User testUser;
    private Game testGame;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("testuser")
                .wins(5)
                .role(UserRole.USER)
                .losses(0)
                .firstName("Test")
                .lastName("User")
                .build();
        testGame = new Game("REACT");
        testGame.setId(5L);
        testGame.setUser(testUser);
        testGame.setGameStatus(GameStatus.PROCEED);
        testGame.setCurrentTry(0);
    }

    @Test
    void proceedGuess_shouldWinGame_whenGuessIsCorrect() {
        //arrange
        when(gameRepository.findById(5L)).thenReturn(Optional.of(testGame));

        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);

        try(MockedStatic<WordleUtils> mockedUtils = mockStatic(WordleUtils.class)) {
            List<LetterStatus> correctStatuses = List.of(
                    LetterStatus.CORRECT,
                    LetterStatus.CORRECT,
                    LetterStatus.CORRECT,
                    LetterStatus.CORRECT,
                    LetterStatus.CORRECT
            );
            mockedUtils.when(() -> WordleUtils.determineLetterStatuses(anyString(), anyString()))
                    .thenReturn(correctStatuses);
            //act
            GuessResponseDTO response = gameService.processGuess(5L, "REACT", authentication);
            //assert
            assertNotNull(response);
            assertEquals(GameStatus.WIN, response.getGameStatus());
            assertEquals(1, response.getCurrentTry());

            verify(userRepository, times(1)).save(any(User.class));
            verify(gameRepository, times(1)).save(any(Game.class));
            verify(letterStatusesRepository, times(1)).saveAll(any());
            verify(attemptRepository, times(1)).save(any(Attempt.class));

            assertEquals(6, testUser.getWins());
        }
    }
}
