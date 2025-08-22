package abs.wordle.backend.repository;

import abs.wordle.backend.models.LetterStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LetterStatusesRepository extends JpaRepository<LetterStatusEntity, Long> {
}
