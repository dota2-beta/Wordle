package abs.wordle.backend.models;

import abs.wordle.backend.enums.GameStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "users_id", nullable = true)
    private User user;

    @JsonManagedReference
    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attempt> attempts;

    private int currentTry;

    private GameStatus gameStatus;

    public Game(String word) {
        this.word = word;
        this.attempts = new ArrayList<>();
        this.currentTry = 0;
        this.gameStatus = GameStatus.PROCEED;
        this.user = null;
    }

    public Game(String word, User user) {
        this.word = word;
        this.user = user;
        this.attempts = new ArrayList<>();
        this.currentTry = 0;
        this.gameStatus = GameStatus.PROCEED;
    }
}