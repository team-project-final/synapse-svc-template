from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.v1 import recommendation
from app.core.exceptions import register_handlers
from app.kafka.consumer import start_in_background


@asynccontextmanager
async def lifespan(app: FastAPI):
    # W3 추가: Kafka 컨슈머를 백그라운드 태스크로 실행
    start_in_background()
    yield
    # shutdown 정리는 컨슈머 측에서 처리


app = FastAPI(title="synapse-learning-svc-ai", version="0.3.0", lifespan=lifespan)

register_handlers(app)

app.include_router(recommendation.router, prefix="/api/v1/recommendations", tags=["recommendation"])


@app.get("/actuator/health")
def health() -> dict:
    return {"status": "UP"}
