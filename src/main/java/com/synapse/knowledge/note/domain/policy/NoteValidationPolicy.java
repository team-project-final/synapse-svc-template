package com.synapse.knowledge.note.domain.policy;

public final class NoteValidationPolicy {

    private static final int MAX_TITLE_LENGTH = 200;

    private NoteValidationPolicy() {}

    public static boolean isValidTitle(String title) {
        return title != null && !title.isBlank() && title.length() <= MAX_TITLE_LENGTH;
    }
}
