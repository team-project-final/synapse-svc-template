from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.api.v1 import recommendation
from app.core.exceptions import register_handlers
from app.infrastructure.messaging.consumer import start_in_background


@asynccontextmanager
async def lifespan(app: FastAPI):
    start_in_background()
    yield


app = FastAPI(title="synapse-learning-svc-ai", version="0.4.0", lifespan=lifespan)

register_handlers(app)

app.include_router(recommendation.router, prefix="/api/v1/recommendations", tags=["recommendation"])


@app.get("/actuator/health")
def health() -> dict:
    return {"status": "UP"}
