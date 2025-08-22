package abs.wordle.backend.services;

import abs.wordle.backend.models.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

@Service
public interface UserService {
    User create(User user);
    User getById(Long id);
    List<User> getAll();
    User getAuthenticated(UserDetails userDetails);
    LinkedHashMap<String, Integer> getTop20Users();
}
