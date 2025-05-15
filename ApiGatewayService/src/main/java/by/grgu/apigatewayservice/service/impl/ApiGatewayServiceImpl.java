package by.grgu.apigatewayservice.service.impl;

import by.grgu.apigatewayservice.database.entity.UserToken;
import by.grgu.apigatewayservice.database.repository.UserTokenRepository;
import by.grgu.apigatewayservice.service.ApiGatewayService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ApiGatewayServiceImpl implements ApiGatewayService {

    private final UserTokenRepository userTokenRepository;

    @Autowired
    public ApiGatewayServiceImpl(UserTokenRepository userTokenRepository) {
        this.userTokenRepository = userTokenRepository;
    }

    @Override
    @Transactional
    public void saveToken(String username, String token) {
        userTokenRepository.deleteByUsername(username);

        UserToken userToken = new UserToken();
        userToken.setUsername(username);
        userToken.setToken(token);
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        userTokenRepository.save(userToken);
    }
}