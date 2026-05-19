"""통일 응답 봉투 — Java의 ApiResponse<T>와 정확히 같은 모양."""

from datetime import datetime, timezone
from typing import Generic, TypeVar

from pydantic import BaseModel

T = TypeVar("T")


class ApiError(BaseModel):
    code: str
    message: str


class ApiResponse(BaseModel, Generic[T]):
    success: bool
    data: T | None = None
    error: ApiError | None = None
    timestamp: datetime

    @classmethod
    def ok(cls, data: T) -> "ApiResponse[T]":
        return cls(success=True, data=data, timestamp=datetime.now(timezone.utc))

    @classmethod
    def fail(cls, code: str, message: str) -> "ApiResponse[None]":
        return cls(
            success=False,
            error=ApiError(code=code, message=message),
            timestamp=datetime.now(timezone.utc),
        )
