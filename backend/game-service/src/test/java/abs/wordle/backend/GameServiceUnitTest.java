package abs.wordle.backend;

import abs.wordle.backend.clients.UserClient;
import abs.wordle.backend.dto.UserDto;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.exceptions.AccessDeniedException;
import abs.wordle.backend.exceptions.GameNotFoundException;
import abs.wordle.backend.exceptions.GuessLengthException;
import abs.wordle.backend.exceptions.InvalidGuessException;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.producers.GameEventProducer;
import abs.wordle.backend.repository.AttemptRepository;
import abs.wordle.backend.repository.GameRepository;
import abs.wordle.backend.repository.LetterStatusesRepository;
import abs.wordle.backend.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceUnitTest {

    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private LetterStatusesRepository letterStatusesRepository;
    @Mock
    private GameEventProducer gameEventProducer;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserClient userClient;
    @InjectMocks
    private GameService gameService;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 59L;
    }

    @Test
    void create_shouldReturnCorrectGame_withUserId() {
        //arrange
        when(userClient.getUserById(userId)).thenReturn(Optional.of(new UserDto(userId, "test")));
        //act
        gameService.create(userId);
        //assert
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());

        Game savedGame = gameCaptor.getValue();

        assertNotNull(savedGame);
        assertEquals(userId, savedGame.getUserId());
        assertNotNull(savedGame.getWord());
        assertEquals(GameStatus.PROCEED, savedGame.getGameStatus());
    }

    @Test
    void processGuess_shouldSendGameFinishedEvent_whenGameIsWon() {
        //arrange
        String word = "GRAVE";
        Long gameId = 12L;
        Game testGame = new Game(word);
        testGame.setId(gameId);
        testGame.setCurrentTry(5);
        testGame.setGameStatus(GameStatus.PROCEED);
        testGame.setUserId(99L);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        //act
        gameService.processGuess(gameId, word, 99L);
        //assert
        verify(gameEventProducer, times(1)).sendGameFinishedMessage(testGame);
    }

    @Test
    void processGuess_shouldThrowInvalidGuessException_whenGuessIsNotAValidWord() {
        //arrange
        String word = "tasty";
        Long gameId = 12L;
        Game testGame = new Game("GRAVE");
        testGame.setId(gameId);
        testGame.setCurrentTry(5);
        testGame.setGameStatus(GameStatus.PROCEED);
        testGame.setUserId(99L);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        //act && assert
        assertThrows(InvalidGuessException.class, () -> gameService.processGuess(gameId, word, 99L));
    }

    @Test
    void proceedGuess_shouldThrowAccessDeniedException_whenUserIsNotGameOwner() {
        //arrange
        String word = "grave";
        Long gameId = 12L;
        Game testGame = new Game(word);
        testGame.setId(gameId);
        testGame.setCurrentTry(5);
        testGame.setGameStatus(GameStatus.PROCEED);
        testGame.setUserId(99L);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        //act && assert
        assertThrows(AccessDeniedException.class, () -> gameService.processGuess(gameId, word, 1L));
    }

    @Test
    void processGuess_shouldThrowGuessLengthException_whenGuessHasTooManyLetters() {
        //arrange
        String word = "tastyyyy";
        Long gameId = 12L;
        Game testGame = new Game("GRAVE");
        testGame.setId(gameId);
        testGame.setCurrentTry(5);
        testGame.setGameStatus(GameStatus.PROCEED);
        testGame.setUserId(99L);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(testGame));
        //act && assert
        assertThrows(GuessLengthException.class, () -> gameService.processGuess(gameId, word, 99L));
    }
}

