package com.synapse.engagement.community.domain.policy;

public final class PostValidationPolicy {

    private static final int MAX_TITLE = 200;

    private PostValidationPolicy() {}

    public static boolean isValidTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= MAX_TITLE;
    }
}
