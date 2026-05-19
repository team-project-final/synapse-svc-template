"""
추천 API — W2에서 ApiResponse 봉투 적용.
W3에서 Java↔Python 통신은 모두 Kafka 이벤트로 전환됩니다.
"""

from fastapi import APIRouter

from app.core.response import ApiResponse
from app.models.recommendation import RecommendationRequest, RecommendationResponse
from app.services.recommendation_service import recommend_cards

router = APIRouter()


@router.post("", response_model=ApiResponse[RecommendationResponse])
def recommend(req: RecommendationRequest) -> ApiResponse[RecommendationResponse]:
    items = recommend_cards(req.user_id, req.context, req.top_k)
    return ApiResponse.ok(RecommendationResponse(items=items))
