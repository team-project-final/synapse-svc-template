"""Java SrsService가 발행한 추천 요청을 수신.

W3 핵심: 양쪽이 HTTP 직접 호출 대신 Kafka 토픽을 통해 비동기 통신.
"""

import asyncio
import json
import logging
from datetime import datetime, timezone

from aiokafka import AIOKafkaConsumer

from app.core.config import get_settings
from app.kafka.events import SrsRecommendationReady, SrsRecommendationRequest
from app.kafka.producer import publish_recommendation_ready
from app.services.recommendation_service import recommend_cards

log = logging.getLogger(__name__)

TOPIC_REQUEST = "synapse.learning.srs.recommendation-request.v1"
GROUP_ID = "synapse-learning-ai"


async def consume_recommendation_requests() -> None:
    settings = get_settings()
    consumer = AIOKafkaConsumer(
        TOPIC_REQUEST,
        bootstrap_servers=settings.kafka_bootstrap,
        group_id=GROUP_ID,
        auto_offset_reset="earliest",
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
    )
    await consumer.start()
    try:
        async for msg in consumer:
            await _handle_request(SrsRecommendationRequest.model_validate(msg.value))
    finally:
        await consumer.stop()


async def _handle_request(req: SrsRecommendationRequest) -> None:
    log.info("SrsRecommendationRequest: requestId=%s userId=%s", req.requestId, req.userId)
    items = recommend_cards(req.userId, req.context, req.topK)
    ready = SrsRecommendationReady(
        requestId=req.requestId,
        userId=req.userId,
        recommendedCardIds=[i.card_id for i in items],
        readyAt=datetime.now(timezone.utc),
    )
    await publish_recommendation_ready(ready)


def start_in_background() -> None:
    """FastAPI startup 이벤트에서 호출 — 백그라운드 태스크로 컨슈머 실행."""
    asyncio.create_task(consume_recommendation_requests())
