package abs.wordle.backend.dto;
import lombok.Data;

@Data
public class GuessRequestDTO {
    private Long gameId;
    private String guess;
}
