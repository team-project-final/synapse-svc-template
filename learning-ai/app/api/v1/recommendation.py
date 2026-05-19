"""
추천 API — Spring(Java) 서비스가 호출하는 추론 엔드포인트.

W1에서는 HTTP 직접 호출이 가능하지만 (테스트용),
W3에서 Java↔Python 통신은 모두 Kafka 이벤트로 전환됩니다.
"""

from fastapi import APIRouter

from app.models.recommendation import RecommendationRequest, RecommendationResponse
from app.services.recommendation_service import recommend_cards

router = APIRouter()


@router.post("", response_model=RecommendationResponse)
def recommend(req: RecommendationRequest) -> RecommendationResponse:
    items = recommend_cards(req.user_id, req.context, req.top_k)
    return RecommendationResponse(items=items)
