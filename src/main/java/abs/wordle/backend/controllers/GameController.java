package abs.wordle.backend.controllers;

import abs.wordle.backend.dto.GameResponseDTO;
import abs.wordle.backend.dto.GuessRequestDTO;
import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.services.GameServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final GameServiceImpl gameService;

    @GetMapping("/{id}")
    public GameResponseDTO getGameById(@PathVariable int id) {
        return new GameResponseDTO(gameService.getGameById(id));
    }

    @GetMapping("/create")
    public GameResponseDTO createGame(Authentication authentication) {
        return new GameResponseDTO(gameService.create(authentication));
    }

    @GetMapping("/all")
    public List<GameResponseDTO> getAllGames() {
        List<Game> games = gameService.findAll();
        return games.stream()
                .map(GameResponseDTO::new)
                .collect(Collectors.toList());
    }

    @PostMapping("/guess")
    public GuessResponseDTO makeGuess ( @RequestBody GuessRequestDTO guessRequestDTO
                                      , Authentication authentication ) {
        return gameService.processGuess ( guessRequestDTO.getGameId()
                                        , guessRequestDTO.getGuess().toUpperCase()
                                        , authentication );
    }
}
