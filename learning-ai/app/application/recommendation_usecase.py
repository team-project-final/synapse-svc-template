"""Use case 함수 — Java의 @Service에 해당.

application은 port(인터페이스)와 domain만 의존. infrastructure는 모름.
"""

from app.application.port import RecommendationPort
from app.application.port_registry import recommendation_port
from app.domain.models import RecommendationRequest, RecommendationResponse, RecommendedItem
from app.domain.policies import RecommendationPolicy


def recommend(req: RecommendationRequest, port: RecommendationPort | None = None) -> RecommendationResponse:
    if not RecommendationPolicy.is_valid_top_k(req.top_k):
        # 기본 보정
        top_k = RecommendationPolicy.clamp_top_k(req.top_k)
    else:
        top_k = req.top_k

    p = port or recommendation_port()
    items: list[RecommendedItem] = p.score(req.user_id, req.context, top_k)
    return RecommendationResponse(items=items)
