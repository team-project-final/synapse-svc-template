"""Recommendation API — api 계층은 application만 호출."""

from fastapi import APIRouter

from app.application.recommendation_usecase import recommend
from app.core.response import ApiResponse
from app.domain.models import RecommendationRequest, RecommendationResponse

router = APIRouter()


@router.post("", response_model=ApiResponse[RecommendationResponse])
def recommend_endpoint(req: RecommendationRequest) -> ApiResponse[RecommendationResponse]:
    return ApiResponse.ok(recommend(req))
