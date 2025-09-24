package abs.wordle.backend.services;

import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCleanupService {
    private final GameRepository gameRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupCompletedGames() {
        log.info("Cleaning up completed games");
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
        int deletedCount = gameRepository.deleteCompletedGamesOlderThan(cutoff, GameStatus.PROCEED);
        log.info("Deleted {} Games", deletedCount);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupAbandonedGames() {
        log.info("Cleaning up abandoned games");
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(60);
        int deletedCount = gameRepository.deleteAbandonedGamesOlderThan(cutoff, GameStatus.PROCEED);
        log.info("Deleted {} Abandoned Games", deletedCount);
    }
}
