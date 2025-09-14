package abs.wordle.backend.repository;

import abs.wordle.backend.enums.GameStatus;
import abs.wordle.backend.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    @Modifying
    @Query("DELETE FROM Game g WHERE g.updatedAt < :cutoff AND g.gameStatus <> :status")
    int deleteCompletedGamesOlderThan(@Param("cutoff") LocalDateTime cutoff, @Param("status") GameStatus status);


    @Modifying
    @Query("DELETE FROM Game g WHERE g.updatedAt < :cutoff AND g.gameStatus = :status")
    int deleteAbandonedGamesOlderThan(@Param("cutoff") LocalDateTime cutoff, @Param("status") GameStatus status);
}
