package abs.wordle.userservice.services;

import abs.wordle.userservice.models.User;
import abs.wordle.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User create(User user) {
        return userRepository.save(user);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User getAuthenticated(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Something went wrong"));
    }

    public LinkedHashMap<String, Integer> getTop20Users() {
        return userRepository.findAllByOrderByWinsDesc(PageRequest.of(0, 20))
                .stream().collect(
                        Collectors.toMap(
                                User::getUsername,
                                User::getWins,
                                (existingValue, newValue) -> existingValue,
                                LinkedHashMap::new
                        ));
    }

    public Long getUserRank(String username) {
        return userRepository.findUserRankByUsername(username).orElseThrow(
                () -> new RuntimeException("User not found")
        );
    }
}