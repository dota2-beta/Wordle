package abs.wordle.backend.services;

import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.exceptions.*;
import abs.wordle.backend.models.Attempt;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.models.LetterStatusEntity;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.models.User;
import abs.wordle.backend.repository.AttemptRepository;
import abs.wordle.backend.repository.GameRepository;
import abs.wordle.backend.repository.LetterStatusesRepository;
import abs.wordle.backend.repository.UserRepository;
import abs.wordle.backend.utils.WordleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final static int MAX_CURRENT_TRY = 6;
    private final static int WORD_LENGTH = 5;

    private final GameRepository gameRepository;
    private final AttemptRepository attemptRepository;
    private final LetterStatusesRepository letterStatusesRepository;
    private final List<String> words = loadWordsFromFile();
    private final UserRepository userRepository;

    public Game create(Authentication authentication) {
        Game game;
        if ( authentication != null && authentication.isAuthenticated()
           && !(authentication instanceof AnonymousAuthenticationToken) ) {
            game = createForAuthenticated(authentication);
        }
        else {
            game = createWithoutAuthenticated();
        }
        return game;
    }

    private Game createForAuthenticated(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException ("Authenticated user not found in DB, data inconsistency?"));
        return gameRepository.save(new Game(getRandomWord(), user));
    }

    private Game createWithoutAuthenticated() {
        return gameRepository.save(new Game(getRandomWord()));
    }

    public Game getGameById(long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found with id: " + id));
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    @Transactional
    public GuessResponseDTO processGuess(long gameId, String guess, Authentication authentication) {
        checkAccess(gameId, authentication);

        Game game = getGameById(gameId);
        User user = game.getUser();

        if (game.getGameStatus() != GameStatus.PROCEED) {
            throw new GameAlreadyFinishedException("Game is already finished");
        }
        if (guess.length() != WORD_LENGTH) {
            throw new GuessLengthException("The word must contain " + WORD_LENGTH + " letters");
        }
        if (!checkGuessExist(guess)) {
            throw new InvalidGuessException("There's no such word in our dictionary");
        }
        game.setCurrentTry(game.getCurrentTry() + 1);

        List<LetterStatus> letterStatuses = saveAttemptAndLetterStatuses(game, guess);

        boolean gameWon = game.getWord().equals(guess);
        boolean gameLost = !gameWon && game.getCurrentTry() >= MAX_CURRENT_TRY;

        if (gameWon) {
            game.setGameStatus(GameStatus.WIN);
            if (user != null) {
                user.setWins(user.getWins() + 1);
                user.setPosition(getUserPosition(user));
                userRepository.save(user);
            }
        } else if (gameLost) {
            game.setGameStatus(GameStatus.LOSE);
            if (user != null) {
                user.setLosses(user.getLosses() + 1);
                user.setPosition(getUserPosition(user));
                userRepository.save(user);
            }
        }
        gameRepository.save(game);
        return new GuessResponseDTO(game, guess, letterStatuses);
    }

    public Long getUserPosition(User user) {
        return userRepository.countByWinsGreaterThan(user.getWins());
    }

    private List<LetterStatus> saveAttemptAndLetterStatuses(Game game, String guess) {
        List<LetterStatus> letterStatuses = WordleUtils.determineLetterStatuses(game.getWord(), guess);

        Attempt attempt = new Attempt();
        attempt.setGame(game);
        attempt.setGuess(guess);

        attemptRepository.save(attempt);

        List<LetterStatusEntity> letterStatusesEntities = new ArrayList<>();
        for(int i = 0; i < letterStatuses.size(); i++) {
            LetterStatusEntity letterStatusEntity = new LetterStatusEntity();
            letterStatusEntity.setAttempt(attempt);
            letterStatusEntity.setLetter(guess.charAt(i));
            letterStatusEntity.setLetterStatus(letterStatuses.get(i));
            letterStatusEntity.setPosition(i);
            letterStatusesEntities.add(letterStatusEntity);
        }
        letterStatusesRepository.saveAll(letterStatusesEntities);

        return letterStatuses;
    }

    private boolean checkGuessExist(String word) {
        return words.contains(word);
    }

    private void checkAccess(Long gameId, Authentication authentication) {
        Game game = getGameById(gameId);
        User gameOwner = game.getUser();
        String currentUsername = null;


        if (  authentication != null
           && authentication.isAuthenticated()
           && !(authentication instanceof AnonymousAuthenticationToken)
           )
        {
            currentUsername = authentication.getName();
        }

        if (gameOwner != null) {
            if (currentUsername == null || !currentUsername.equals(gameOwner.getUsername()))
                throw new AccessDeniedException("You are not allowed to access this game.");
        }
        else
            if (currentUsername != null)
                throw new AccessDeniedException("You are not allowed to access this game.");

    }

    private List<String> loadWordsFromFile() {
        try (InputStream inputStream = new ClassPathResource("words.json").getInputStream()) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> words = objectMapper.readValue(inputStream,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            return words.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load words from file", e);
        }
    }

    private String getRandomWord() {
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }
}
