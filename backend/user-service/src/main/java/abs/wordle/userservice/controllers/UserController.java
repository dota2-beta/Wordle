package abs.wordle.userservice.controllers;

import abs.wordle.userservice.dto.UserInternalDto;
import abs.wordle.userservice.dto.UserResponseDTO;
import abs.wordle.userservice.models.User;
import abs.wordle.userservice.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{id}")
    public User getUser(@PathVariable  Long id) {
        return userService.getById(id);
    }

    @GetMapping("/")
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/profile")
    public UserResponseDTO getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getAuthenticated(userDetails);
        Long rank = userService.getUserRank(userDetails.getUsername());
        return new UserResponseDTO(user, rank);
    }

    @GetMapping("/top")
    public Map<String, Integer> getTop20Users() {
        return userService.getTop20Users();
    }

    @GetMapping("/me/rank")
    public ResponseEntity<Map<String, Long>> getMyRank(Authentication authentication) {
        String currentUsername = authentication.getName();
        Long rank = userService.getUserRank(currentUsername);

        Map<String, Long> response = new HashMap<>();
        response.put("rank", rank);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<UserInternalDto> getUserForInternalCommunication(@PathVariable Long id) {
        User user = userService.getById(id);
        UserInternalDto dto = new UserInternalDto(user.getId(), user.getUsername());
        return ResponseEntity.ok(dto);
    }
}
