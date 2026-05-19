"""Port → Adapter 바인딩.

infrastructure를 직접 import하는 유일한 application 모듈.
import-linter 룰에서 이 파일만 예외 처리.
"""

from app.application.port import RecommendationPort
from app.infrastructure.ml.recommendation_adapter import RecommendationMlAdapter

_recommendation_port: RecommendationPort = RecommendationMlAdapter()


def recommendation_port() -> RecommendationPort:
    return _recommendation_port
