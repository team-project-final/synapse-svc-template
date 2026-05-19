package com.synapse.platform.auth.service;

import com.synapse.platform.auth.dto.request.LoginRequest;
import com.synapse.platform.auth.dto.response.TokenResponse;
import com.synapse.platform.auth.kafka.producer.UserEventPublisher;
import com.synapse.platform.auth.repository.UserRepository;
import com.synapse.platform.global.exception.BusinessException;
import com.synapse.platform.global.exception.ErrorCode;
import com.synapse.platform.global.kafka.event.UserRegistered;
import com.synapse.platform.global.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserEventPublisher userEventPublisher;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.userEventPublisher = userEventPublisher;
    }

    public TokenResponse login(LoginRequest request) {
        throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "W3 스텁 — 실제 인증은 user 도메인 채워진 후");
    }

    /**
     * 신규 가입 플로우 데모 — DB 저장 후 UserRegistered 이벤트 발행.
     * notification 도메인은 직접 호출되지 않고 Kafka 컨슈머가 환영 알림 발송.
     */
    public void registerDemo(Long userId, String email) {
        userEventPublisher.publishUserRegistered(
            new UserRegistered(userId, email, Instant.now())
        );
    }
}
