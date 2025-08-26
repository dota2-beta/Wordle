package abs.wordle.backend;

import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.repository.GameRepository;
import static org.junit.jupiter.api.Assertions.*;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.services.GameServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private GameServiceImpl gameServiceImpl;

    @Test
    void determineLetterStatuses_shouldReturnCorrectStatuses_whenGuessIsComplex() {
        //arrange
        String guess = "ABBEY";
        String word = "BOBBY";
        //ACT
        List<LetterStatus> letterStatuses = gameServiceImpl.determineLetterStatuses(word, guess);
        //assert
        assertNotNull(letterStatuses);
        assertEquals(5, letterStatuses.size());
        assertEquals(LetterStatus.INCORRECT, letterStatuses.get(0));
        assertEquals(LetterStatus.MISPLACED, letterStatuses.get(1));
        assertEquals(LetterStatus.CORRECT, letterStatuses.get(2));
        assertEquals(LetterStatus.INCORRECT, letterStatuses.get(3));
        assertEquals(LetterStatus.CORRECT, letterStatuses.get(4));
    }

    @Test
    void determineLetterStatuses_shouldReturnAllCorrect_whenGuessIsPerfect() {
        String guess = "INDEX";
        String word = "INDEX";

        List<LetterStatus> letterStatuses = gameServiceImpl.determineLetterStatuses(word, guess);
        assertNotNull(letterStatuses);
        assertTrue(letterStatuses.stream().allMatch(s -> s.equals(LetterStatus.CORRECT)));
    }
}
