"""추천 비즈니스 로직 — 스텁."""

from app.models.recommendation import RecommendedItem


def recommend_cards(user_id: int, context: str | None, top_k: int) -> list[RecommendedItem]:
    # W1 스텁: 실제 추천은 임베딩·랭킹 모델 사용. W4에서 ml/ 모듈로 분리 예정.
    return [RecommendedItem(card_id=i, score=1.0 - (i * 0.1)) for i in range(1, top_k + 1)]
