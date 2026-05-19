"""이벤트 스키마 — Java의 global/kafka/event/*와 1:1 매핑."""

from datetime import datetime

from pydantic import BaseModel


class SrsRecommendationRequest(BaseModel):
    requestId: str
    userId: int
    context: str | None = None
    topK: int = 5
    requestedAt: datetime


class SrsRecommendationReady(BaseModel):
    requestId: str
    userId: int
    recommendedCardIds: list[int]
    readyAt: datetime
