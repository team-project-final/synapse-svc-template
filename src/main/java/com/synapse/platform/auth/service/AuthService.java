package com.synapse.platform.auth.service;

import com.synapse.platform.auth.dto.request.LoginRequest;
import com.synapse.platform.auth.dto.response.TokenResponse;
import com.synapse.platform.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public TokenResponse login(LoginRequest request) {
        // TODO: 인증 로직 (W2 SecurityConfig + JwtTokenProvider 도입 후 구현)
        return new TokenResponse("stub-access-token", "stub-refresh-token");
    }
}
