package abs.wordle.backend.controllers;

import abs.wordle.backend.dto.GameResponseDTO;
import abs.wordle.backend.dto.GuessRequestDTO;
import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.services.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/{id}")
    public GameResponseDTO getGameById(@PathVariable int id) {
        return new GameResponseDTO(gameService.getGameById(id));
    }

    @PostMapping("/create")
    public GameResponseDTO createGame(
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if(userId == null)
            return new GameResponseDTO(gameService.create(null));
        return new GameResponseDTO(gameService.create(Long.parseLong(userId)));
    }

    @GetMapping("/all")
    public List<GameResponseDTO> getAllGames() {
        List<Game> games = gameService.findAll();
        return games.stream()
                .map(GameResponseDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/guess")
    public GuessResponseDTO makeGuess (@RequestBody GuessRequestDTO guessRequestDTO,
                                       @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Long gameId = guessRequestDTO.getGameId();
        String guess = guessRequestDTO.getGuess().toUpperCase();
        if(userId == null)
            return gameService.processGuess(gameId, guess, null);
        return gameService.processGuess(gameId, guess, Long.parseLong(userId));
    }
}
