package com.synapse.engagement.gamification.infrastructure.messaging;

import com.synapse.engagement.gamification.application.GamificationService;
import com.synapse.engagement.gamification.domain.policy.PointPolicy;
import com.synapse.engagement.global.kafka.event.NoteCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class NoteCreatedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedKafkaConsumer.class);

    private final GamificationService gamificationService;

    NoteCreatedKafkaConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated → 작성 포인트: userId={}", event.ownerId());
        gamificationService.awardForEvent(event.ownerId(), PointPolicy.REASON_NOTE_WRITE, 0);
    }
}
