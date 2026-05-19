"""Java SrsService가 발행한 추천 요청을 수신."""

import asyncio
import json
import logging
from datetime import datetime, timezone

from aiokafka import AIOKafkaConsumer

from app.application.recommendation_usecase import recommend
from app.core.config import get_settings
from app.domain.models import RecommendationRequest
from app.infrastructure.messaging.events import SrsRecommendationReady, SrsRecommendationRequest
from app.infrastructure.messaging.producer import publish_recommendation_ready

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
            req = SrsRecommendationRequest.model_validate(msg.value)
            log.info("SrsRecommendationRequest: requestId=%s", req.requestId)
            response = recommend(RecommendationRequest(user_id=req.userId, context=req.context, top_k=req.topK))
            ready = SrsRecommendationReady(
                requestId=req.requestId,
                userId=req.userId,
                recommendedCardIds=[i.card_id for i in response.items],
                readyAt=datetime.now(timezone.utc),
            )
            await publish_recommendation_ready(ready)
    finally:
        await consumer.stop()


def start_in_background() -> None:
    asyncio.create_task(consume_recommendation_requests())
