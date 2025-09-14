package abs.wordle.userservice.dto;

import abs.wordle.userservice.enums.UserRole;
import abs.wordle.userservice.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String username;
    private UserRole role;
    private Integer wins;
    private Integer losses;
    private Long position;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;

    public UserResponseDTO(User user, Long userPosition) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.wins = user.getWins();
        this.losses = user.getLosses();
        this.position = userPosition;
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.createdAt = user.getCreatedAt();
    }
}
