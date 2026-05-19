package com.synapse.engagement.gamification.kafka.consumer;

import com.synapse.engagement.gamification.service.GamificationService;
import com.synapse.engagement.global.kafka.event.NoteCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * knowledge 서비스의 노트 생성 이벤트 → 작성 포인트.
 */
@Component
public class NoteCreatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(NoteCreatedConsumer.class);

    private final GamificationService gamificationService;

    public NoteCreatedConsumer(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @KafkaListener(
        topics = "synapse.knowledge.note.created.v1",
        groupId = "synapse-engagement-gamification"
    )
    public void onNoteCreated(NoteCreated event) {
        log.info("NoteCreated → 작성 포인트: userId={}", event.ownerId());
        gamificationService.awardPoint(event.ownerId(), 20, "NOTE_WRITE");
    }
}
