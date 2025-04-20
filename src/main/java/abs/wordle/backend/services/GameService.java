package abs.wordle.backend.services;

import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.models.User;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface GameService {
    Game create(Authentication authentication);
    Game getGameById(long id);
    List<Game> findAll();
    GuessResponseDTO processGuess(long gameId, String guess, Authentication authentication);
    Long getUserPosition(User user);
}
