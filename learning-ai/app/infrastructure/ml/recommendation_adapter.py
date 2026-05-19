"""RecommendationPort 구현 — 실제 ML 모델 호출.

W4에서는 스텁. 추후 임베딩·랭킹 모델로 교체.
"""

from app.application.port import RecommendationPort
from app.domain.models import RecommendedItem


class RecommendationMlAdapter(RecommendationPort):
    def score(self, user_id: int, context: str | None, top_k: int) -> list[RecommendedItem]:
        # TODO: 실제 임베딩 → 유사도 랭킹
        return [RecommendedItem(card_id=i, score=1.0 - (i - 1) * 0.1) for i in range(1, top_k + 1)]
