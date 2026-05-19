"""Python → Kafka 발행."""

import json
import logging

from aiokafka import AIOKafkaProducer

from app.core.config import get_settings
from app.infrastructure.messaging.events import SrsRecommendationReady

log = logging.getLogger(__name__)

TOPIC_READY = "synapse.learning.ai.recommendation-ready.v1"

_producer: AIOKafkaProducer | None = None


async def _get_producer() -> AIOKafkaProducer:
    global _producer
    if _producer is None:
        settings = get_settings()
        _producer = AIOKafkaProducer(
            bootstrap_servers=settings.kafka_bootstrap,
            value_serializer=lambda v: json.dumps(v, default=str).encode("utf-8"),
            acks="all",
        )
        await _producer.start()
    return _producer


async def publish_recommendation_ready(event: SrsRecommendationReady) -> None:
    producer = await _get_producer()
    await producer.send_and_wait(TOPIC_READY, event.model_dump(mode="json"), key=str(event.userId).encode())
    log.info("Published SrsRecommendationReady: requestId=%s", event.requestId)
