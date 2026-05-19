package com.synapse.platform.auth.application;

import com.synapse.platform.auth.api.dto.request.LoginRequest;
import com.synapse.platform.auth.api.dto.response.TokenResponse;
import com.synapse.platform.auth.application.port.EventPort;
import com.synapse.platform.auth.application.port.UserPort;
import com.synapse.platform.auth.domain.User;
import com.synapse.platform.global.exception.BusinessException;
import com.synapse.platform.global.exception.ErrorCode;
import com.synapse.platform.global.kafka.event.UserRegistered;
import com.synapse.platform.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserPort userPort;
    private final EventPort eventPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserPort userPort,
                       EventPort eventPort,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userPort = userPort;
        this.eventPort = eventPort;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse login(LoginRequest request) {
        User user = userPort.findByEmail(request.email())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String accessToken = tokenProvider.issueAccessToken(user.getId().toString());
        return new TokenResponse(accessToken, "TODO-refresh-token");
    }

    public void registerDemo(Long userId, String email) {
        eventPort.publishUserRegistered(new UserRegistered(userId, email, Instant.now()));
    }
}
