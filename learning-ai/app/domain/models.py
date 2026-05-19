"""도메인 모델 — DB·HTTP·Kafka 무관한 비즈니스 데이터."""

from pydantic import BaseModel, Field


class RecommendationRequest(BaseModel):
    user_id: int
    context: str | None = None
    top_k: int = Field(default=5, ge=1, le=50)


class RecommendedItem(BaseModel):
    card_id: int
    score: float


class RecommendationResponse(BaseModel):
    items: list[RecommendedItem]
