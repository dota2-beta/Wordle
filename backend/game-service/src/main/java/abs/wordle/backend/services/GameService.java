package abs.wordle.backend.services;

import abs.wordle.backend.clients.UserClient;
import abs.wordle.backend.dto.GuessResponseDTO;
import abs.wordle.backend.dto.UserDto;
import abs.wordle.backend.exceptions.*;
import abs.wordle.backend.models.Attempt;
import abs.wordle.backend.models.Game;
import abs.wordle.backend.models.LetterStatusEntity;
import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.enums.LetterStatus;
import abs.wordle.backend.producers.GameEventProducer;
import abs.wordle.backend.repository.AttemptRepository;
import abs.wordle.backend.repository.GameRepository;
import abs.wordle.backend.repository.LetterStatusesRepository;
import abs.wordle.backend.utils.WordleUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {
    private final static int MAX_CURRENT_TRY = 6;
    private final static int WORD_LENGTH = 5;

    private final GameEventProducer gameEventProducer;
    private final GameRepository gameRepository;
    private final AttemptRepository attemptRepository;
    private final LetterStatusesRepository letterStatusesRepository;
    private final UserClient userClient;
    private final List<String> words = loadWordsFromFile();

    public Game create(Long userId) {
        if(userId != null) {
            UserDto userDto = userClient.getUserById(userId).orElseThrow(
                    () -> new IllegalArgumentException("User with not found"));
            return gameRepository.save(new Game(getRandomWord(), userDto.id()));
        }
        return gameRepository.save(new Game(getRandomWord()));
    }

    public Game getGameById(long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException("Game not found"));
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    @Transactional
    public GuessResponseDTO processGuess(long gameId, String guess, Long userId) {
        Game game = getGameById(gameId);
        if (!Objects.equals(game.getUserId(), userId)) {
            throw new AccessDeniedException("You are not allowed to access this game.");
        }
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
            if (game.getUserId() != null)
                gameEventProducer.sendGameFinishedMessage(game);
        } else if (gameLost) {
            game.setGameStatus(GameStatus.LOSE);
            if (game.getUserId() != null)
                gameEventProducer.sendGameFinishedMessage(game);
        }
        gameRepository.save(game);
        return new GuessResponseDTO(game, guess, letterStatuses);
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
