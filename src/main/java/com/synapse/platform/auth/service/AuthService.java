package com.synapse.platform.auth.service;

import com.synapse.platform.auth.dto.request.LoginRequest;
import com.synapse.platform.auth.dto.response.TokenResponse;
import com.synapse.platform.auth.repository.UserRepository;
import com.synapse.platform.global.exception.BusinessException;
import com.synapse.platform.global.exception.ErrorCode;
import com.synapse.platform.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public TokenResponse login(LoginRequest request) {
        // 실제 구현 예시 — passwordEncoder + tokenProvider 사용
        // var user = userRepository.findByEmail(request.email())
        //     .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        // if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
        //     throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        // }
        // return new TokenResponse(tokenProvider.issueAccessToken(user.getId().toString()), ...);
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "W2 스텁 — 실제 인증은 user 도메인 채워진 후");
    }
}
