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
        // Проверка доступа
        checkAccess(gameId, authentication);

        // Получение игры
        Game game = getGameById(gameId);
        User user = game.getUser(); // Получаем пользователя для обновления статистики

        // Проверка состояния игры и валидности ввода
        if (game.getGameStatus() != GameStatus.PROCEED) {
            throw new GameAlreadyFinishedException("Game is already finished");
        }
        if (guess.length() != WORD_LENGTH) { // Сравниваем с длиной слова в игре
            throw new GuessLengthException("The word must contain " + WORD_LENGTH + " letters");
        }
        if (!checkGuessExist(guess)) {
            throw new InvalidGuessException("There's no such word in our dictionary");
        }

        // Инкремент попытки перед проверкой исхода
        game.setCurrentTry(game.getCurrentTry() + 1);

        // Сохранение попытки и определение статусов букв
        List<LetterStatus> letterStatuses = saveAttemptAndLetterStatuses(game, guess);

        // Определение исхода игры и обновление статистики
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
        // Если ни то, ни другое, статус остается PROCEED

        // Сохранение обновленного состояния игры
        gameRepository.save(game);
        return new GuessResponseDTO(game, guess, letterStatuses);
    }

    public Long getUserPosition(User user) {
        return userRepository.countByWinsGreaterThan(user.getWins());
    }

    private List<LetterStatus> determineLetterStatuses(String word, String guess) {
        List<LetterStatus> letterStatuses = new ArrayList<>();
        HashMap<Character, Integer> letterCounts = new HashMap<>();

        for(char c : word.toCharArray()) {
            letterCounts.put(c, letterCounts.getOrDefault(c, 0) + 1);
        }

        for(int i = 0; i < word.length(); i++) {
            if(word.charAt(i) == guess.charAt(i)) {
                letterStatuses.add(LetterStatus.CORRECT);
                letterCounts.put(word.charAt(i), letterCounts.get(word.charAt(i)) - 1);
            }
            else
                letterStatuses.add(null);
        }

        for(int i = 0; i < word.length(); i++) {
            if (letterStatuses.get(i) == null) {
                int guessChar = word.indexOf(guess.charAt(i));
                if(guessChar == -1)
                    letterStatuses.set(i, LetterStatus.INCORRECT);
                else if (letterCounts.get(guess.charAt(i)) > 0) {
                    letterStatuses.set(i, LetterStatus.MISPLACED);
                    letterCounts.put(guess.charAt(i), letterCounts.get(guess.charAt(i)) - 1);
                }
                else
                    letterStatuses.set(i, LetterStatus.INCORRECT);
            }
        }

        System.out.println(letterStatuses);
        return letterStatuses;
    }

    private List<LetterStatus> saveAttemptAndLetterStatuses(Game game, String guess) {
        List<LetterStatus> letterStatuses = determineLetterStatuses(game.getWord(), guess);

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
