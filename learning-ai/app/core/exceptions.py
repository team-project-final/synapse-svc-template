"""도메인 예외 + ErrorCode + FastAPI 핸들러 — Java의 BusinessException과 동등.

ErrorCode 코드 prefix:
- C___ 공통
- R___ recommendation 도메인
- E___ embedding 도메인 (W3 이후)
"""

from enum import Enum

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.core.response import ApiResponse


class ErrorCode(str, Enum):
    INVALID_REQUEST = ("C001", "잘못된 요청입니다.", 400)
    UNAUTHORIZED = ("C002", "인증이 필요합니다.", 401)
    NOT_FOUND = ("C004", "리소스를 찾을 수 없습니다.", 404)
    INTERNAL_ERROR = ("C999", "서버 오류가 발생했습니다.", 500)

    RECOMMENDATION_FAILED = ("R001", "추천 생성에 실패했습니다.", 500)

    def __new__(cls, code: str, message: str, status: int) -> "ErrorCode":
        obj = str.__new__(cls, code)
        obj._value_ = code
        obj.code = code
        obj.message = message
        obj.status = status
        return obj


class BusinessException(Exception):
    def __init__(self, error_code: ErrorCode, custom_message: str | None = None) -> None:
        self.error_code = error_code
        super().__init__(custom_message or error_code.message)


def register_handlers(app: FastAPI) -> None:
    @app.exception_handler(BusinessException)
    async def business_handler(_: Request, exc: BusinessException) -> JSONResponse:
        resp = ApiResponse.fail(exc.error_code.code, str(exc))
        return JSONResponse(status_code=exc.error_code.status, content=resp.model_dump(mode="json"))

    @app.exception_handler(Exception)
    async def unknown_handler(_: Request, exc: Exception) -> JSONResponse:
        resp = ApiResponse.fail(ErrorCode.INTERNAL_ERROR.code, ErrorCode.INTERNAL_ERROR.message)
        return JSONResponse(status_code=500, content=resp.model_dump(mode="json"))
