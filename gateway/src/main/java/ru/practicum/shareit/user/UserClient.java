package ru.practicum.shareit.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.BaseClient;
import ru.practicum.shareit.user.model.UserDto;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/users";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }
    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post("", null, userDto);
    }
    public ResponseEntity<Object> getUserById(int userId) {
        return get("/" + userId, null, null);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("", null, null);
    }



    public ResponseEntity<Object> updateUser(int userId, UserDto userDto) {
        return patch("/" + userId, null, null, userDto);
    }

    public ResponseEntity<Object> deleteUser(int userId) {
        return delete("/" + userId, null);
    }
}