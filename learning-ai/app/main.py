from fastapi import FastAPI

from app.api.v1 import recommendation

app = FastAPI(title="synapse-learning-svc-ai", version="0.1.0")

app.include_router(recommendation.router, prefix="/api/v1/recommendations", tags=["recommendation"])


@app.get("/actuator/health")
def health() -> dict:
    return {"status": "UP"}
