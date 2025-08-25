package abs.wordle.backend.controllers;

import abs.wordle.backend.dto.UserResponseDTO;
import abs.wordle.backend.models.User;
import abs.wordle.backend.services.UserServiceImpl;
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

    private final UserServiceImpl userService;


    public UserController(UserServiceImpl userService) {
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
        return new UserResponseDTO(userService.getAuthenticated(userDetails));
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
}
