package abs.wordle.backend.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Attempt {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "game_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Game game;

    private String guess;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<LetterStatusEntity> letterStatusesList;

    @JsonGetter("letterStatuses")
    public List<String> getLetterStatuses() {
        return letterStatusesList.stream()
                .map(letterStatusEntity -> letterStatusEntity.getLetterStatus().name())
                .collect(Collectors.toList());
    }
}
