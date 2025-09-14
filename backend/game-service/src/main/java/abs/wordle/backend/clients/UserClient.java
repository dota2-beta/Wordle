package abs.wordle.backend.clients;

import abs.wordle.backend.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplate restTemplate;
    private final String userServiceUrl = "http://user-service:8082/api/users/internal/";

    public Optional<UserDto> getUserById(Long userId) {
        try{
            UserDto user = restTemplate.getForObject(userServiceUrl + userId, UserDto.class);
            return Optional.ofNullable(user);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
