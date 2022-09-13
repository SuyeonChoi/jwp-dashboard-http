package nextstep.jwp.service;

import java.util.Map;
import nextstep.jwp.model.User;
import nextstep.jwp.repository.InMemoryUserRepository;
import org.apache.coyote.http11.request.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final String USER_ACCOUNT = "account";
    private static final String USER_PASSWORD = "password";
    private final Logger log = LoggerFactory.getLogger(getClass());

    public User login(final HttpRequest request) {
        final Map<String, String> bodyParams = request.parseApplicationFormData();
        final String userAccount = bodyParams.get(USER_ACCOUNT);
        final String userPassword = bodyParams.get(USER_PASSWORD);

        final User user = InMemoryUserRepository.findByAccount(userAccount)
                .filter(it -> it.checkPassword(userPassword))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저입니다."));
        log.info("user : {}", user);

        return user;
    }

    public void register(final HttpRequest request) {
        final Map<String, String> bodyParams = request.parseApplicationFormData();
        final String account = bodyParams.get(USER_ACCOUNT);
        final String password = bodyParams.get(USER_PASSWORD);
        final String email = bodyParams.get("email");

        final User user = new User(account, password, email);
        InMemoryUserRepository.save(user);

        log.info("saved user : {}", user);
    }
}
