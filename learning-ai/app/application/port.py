"""Outbound port — infrastructure가 구현.

Python에는 Java의 interface가 없지만 Protocol(PEP 544)로 같은 효과.
"""

from typing import Protocol

from app.domain.models import RecommendedItem


class RecommendationPort(Protocol):
    def score(self, user_id: int, context: str | None, top_k: int) -> list[RecommendedItem]: ...
