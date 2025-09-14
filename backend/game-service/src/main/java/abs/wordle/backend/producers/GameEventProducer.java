package abs.wordle.backend.producers;

import abs.wordle.backend.models.Game;
import abs.wordle.events.GameFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    //@Value("${topic.game-finished}")
    private static String GAME_FINISHED_TOPIC = "game-finished-topic";

    public void sendGameFinishedMessage(Game game) {
        GameFinishedEvent event = GameFinishedEvent.builder()
                  .userId(game.getUserId())
                  .result(game.getGameStatus().name())
                .build();
        kafkaTemplate.send(GAME_FINISHED_TOPIC, game.getId().toString(), event);
    }
}
