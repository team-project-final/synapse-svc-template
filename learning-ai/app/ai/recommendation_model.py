"""실제 추천 모델 로직 — W4에서 services에서 분리.

W3에서는 services/recommendation_service.py가 호출하는 모듈로 자리만 잡습니다.
"""

from app.models.recommendation import RecommendedItem


def score_cards(user_id: int, context: str | None, candidates: list[int]) -> list[RecommendedItem]:
    # 스텁: 실제로는 임베딩·랭킹 모델
    return [RecommendedItem(card_id=c, score=1.0 - i * 0.1) for i, c in enumerate(candidates)]
