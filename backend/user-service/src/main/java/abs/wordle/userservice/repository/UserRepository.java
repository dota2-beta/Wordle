package abs.wordle.userservice.repository;

import abs.wordle.userservice.models.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByOrderByWinsDesc(Pageable pageable);

    Long countByWinsGreaterThan(Integer wins);

    @Query(
            value = "SELECT position FROM (" +
                    "    SELECT username, RANK() OVER (ORDER BY wins DESC) as position " +
                    "    FROM user_schema.users" +
                    ") as users_rank " +
                    "WHERE username = :username",
            nativeQuery = true
    )
    Optional<Long> findUserRankByUsername(@Param("username") String username);
}
