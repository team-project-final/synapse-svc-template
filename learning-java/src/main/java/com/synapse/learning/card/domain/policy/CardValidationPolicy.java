package com.synapse.learning.card.domain.policy;

public final class CardValidationPolicy {

    private static final int MAX_TEXT_LENGTH = 5000;

    private CardValidationPolicy() {}

    public static boolean isValid(String front, String back) {
        if (front == null || front.isBlank() || front.length() > MAX_TEXT_LENGTH) return false;
        if (back == null || back.isBlank() || back.length() > MAX_TEXT_LENGTH) return false;
        return true;
    }
}
