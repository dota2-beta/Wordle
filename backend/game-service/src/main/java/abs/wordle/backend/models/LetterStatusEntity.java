package abs.wordle.backend.models;

import abs.wordle.backend.enums.LetterStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LetterStatusEntity {
    @Id
    @GeneratedValue
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "attempt_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Attempt attempt;

    private char letter;

    @Enumerated(EnumType.STRING)
    private LetterStatus letterStatus;

    private int position;

}
