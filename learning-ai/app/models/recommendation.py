"""Pydantic 모델 — API 입출력 스키마."""

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
