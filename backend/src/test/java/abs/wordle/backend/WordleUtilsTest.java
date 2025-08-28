package abs.wordle.backend;

import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.repository.GameRepository;
import static org.junit.jupiter.api.Assertions.*;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.services.GameServiceImpl;
import abs.wordle.backend.utils.WordleUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class WordleUtilsTest {
    @Test
    void determineLetterStatuses_shouldReturnCorrectStatuses_whenGuessIsComplex() {
        //arrange
        String guess = "ABBEY";
        String word = "BOBBY";
        //ACT
        List<LetterStatus> letterStatuses = WordleUtils.determineLetterStatuses(word, guess);
        //assert
        assertNotNull(letterStatuses);
        assertEquals(5, letterStatuses.size());
        assertEquals(List.of(
                LetterStatus.INCORRECT,
                LetterStatus.MISPLACED,
                LetterStatus.CORRECT,
                LetterStatus.INCORRECT,
                LetterStatus.CORRECT
        ), letterStatuses);
    }

    @Test
    void determineLetterStatuses_shouldReturnAllCorrect_whenGuessIsPerfect() {
        String guess = "INDEX";
        String word = "INDEX";

        List<LetterStatus> letterStatuses = WordleUtils.determineLetterStatuses(word, guess);
        assertNotNull(letterStatuses);
        assertTrue(letterStatuses.stream().allMatch(s -> s.equals(LetterStatus.CORRECT)));
    }

    @Test
    @DisplayName("When guess contains more occurrences of a correct letter than the word, " +
            "then excess letters should be marked as INCORRECT")
    void determineLetterStatuses_handlesExcessLettersCorrectly() {
        String guess = "ABABB";
        String word = "ABBEY";

        List<LetterStatus> letterStatuses = WordleUtils.determineLetterStatuses(word, guess);
        assertEquals(List.of(
                LetterStatus.CORRECT,
                LetterStatus.CORRECT,
                LetterStatus.INCORRECT,
                LetterStatus.MISPLACED,
                LetterStatus.INCORRECT), letterStatuses);
    }
}
