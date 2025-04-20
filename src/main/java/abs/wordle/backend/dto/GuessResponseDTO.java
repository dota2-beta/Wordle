package abs.wordle.backend.dto;

import abs.wordle.backend.models.Game;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.enums.LetterStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuessResponseDTO {
    private long gameId;
    private String guess;
    private List<LetterStatus> letterStatuses;
    private GameStatus gameStatus;
    private int currentTry;

    public GuessResponseDTO(Game game, String guess, List<LetterStatus> letterStatuses) {
        this.gameId = game.getId();
        this.guess = guess;
        this.letterStatuses = letterStatuses;
        this.gameStatus = game.getGameStatus();
        this.currentTry = game.getCurrentTry();
    }
}
