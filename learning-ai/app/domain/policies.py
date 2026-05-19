"""도메인 룰 — 외부 의존 0의 순수 Python.

import-linter 룰에서 `app.domain.policies`는 어떤 외부 패키지도 import하지 않음을 강제.
"""


class RecommendationPolicy:
    MIN_TOP_K = 1
    MAX_TOP_K = 50
    DEFAULT_TOP_K = 5

    @classmethod
    def is_valid_top_k(cls, top_k: int) -> bool:
        return cls.MIN_TOP_K <= top_k <= cls.MAX_TOP_K

    @classmethod
    def clamp_top_k(cls, top_k: int) -> int:
        if top_k < cls.MIN_TOP_K:
            return cls.MIN_TOP_K
        if top_k > cls.MAX_TOP_K:
            return cls.MAX_TOP_K
        return top_k
