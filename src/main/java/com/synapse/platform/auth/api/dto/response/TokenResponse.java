package com.synapse.platform.auth.api.dto.response;

public record TokenResponse(String accessToken, String refreshToken) {
}
