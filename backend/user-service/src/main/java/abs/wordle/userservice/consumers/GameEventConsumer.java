package abs.wordle.userservice.consumers;

import abs.wordle.events.GameFinishedEvent;
import abs.wordle.userservice.models.User;
import abs.wordle.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameEventConsumer {
    private final UserRepository userRepository;
    @KafkaListener(topics = "game-finished-topic",
            groupId = "user-service-group")
    public void handleGameFinished(GameFinishedEvent gameFinishedEvent) {
        log.debug("Received GameFinishedEvent for user {}", gameFinishedEvent);
        Long userId = gameFinishedEvent.getUserId();
        if(userId == null) {
            log.info("Event for anonymous user, skipping stats update.");
            return;
        }
        User user = userRepository.findById(gameFinishedEvent.getUserId()).orElse(null);
        if(user == null) {
            log.warn("User with id {} not found, cannot update stats.", gameFinishedEvent.getUserId());
            return;
        }
        if(gameFinishedEvent.getResult().equals("WIN"))
            user.setWins(user.getWins() + 1);
        else
            user.setLosses(user.getLosses() + 1);
        userRepository.save(user);
        log.info("Successfully updated stats for user: {}", user.getUsername());
    }
}
