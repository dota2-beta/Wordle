package abs.wordle.backend.models;

import abs.wordle.backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue
    private Long id;

    private String word;

    @JoinColumn(name = "users_id", nullable = true)
    private Long userId;

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attempt> attempts;

    private int currentTry;

    private GameStatus gameStatus;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Game(String word) {
        this.word = word;
        this.attempts = new ArrayList<>();
        this.currentTry = 0;
        this.gameStatus = GameStatus.PROCEED;
        this.userId = null;
    }

    public Game(String word, Long userId) {
        this.word = word;
        this.userId = userId;
        this.attempts = new ArrayList<>();
        this.currentTry = 0;
        this.gameStatus = GameStatus.PROCEED;
    }
}