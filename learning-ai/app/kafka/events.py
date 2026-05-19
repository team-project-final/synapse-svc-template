"""이벤트 스키마 — Java의 `global/kafka/event/*` 와 1:1 매핑.

JSON 필드명도 동일하게 유지 (양쪽이 같은 봉투를 직렬화/역직렬화).
Avro 도입 후엔 shared-events에서 생성된 Python 클래스로 교체 예정.
"""

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
