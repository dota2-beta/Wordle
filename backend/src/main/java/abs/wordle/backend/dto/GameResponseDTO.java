package abs.wordle.backend.dto;

import abs.wordle.backend.models.Attempt;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.enums.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponseDTO {
    private Long id;
    private String word;
    private List<Attempt> attempts;
    private int currentTry;
    private GameStatus gameStatus;


    public GameResponseDTO(Game game) {
        this.id = game.getId();
        if (game.getGameStatus() == GameStatus.WIN || game.getGameStatus() == GameStatus.LOSE) {
            this.word = game.getWord();
        } else {
            this.word = null;
        }
        this.currentTry = game.getCurrentTry();
        this.gameStatus = game.getGameStatus();
        this.attempts = game.getAttempts();
    }
}
