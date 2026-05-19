from fastapi import FastAPI

from app.api.v1 import recommendation
from app.core.exceptions import register_handlers

app = FastAPI(title="synapse-learning-svc-ai", version="0.2.0")

register_handlers(app)

app.include_router(recommendation.router, prefix="/api/v1/recommendations", tags=["recommendation"])


@app.get("/actuator/health")
def health() -> dict:
    return {"status": "UP"}
