package abs.wordle.backend.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic gameFinishedTopic() {
        return TopicBuilder.name("game-finished-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
