"""환경 설정 — pydantic-settings로 .env / 환경변수 자동 로드.

Java의 application.yml + Spring Boot ConfigurationProperties와 동일 역할.
"""

from functools import lru_cache

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    app_name: str = "synapse-learning-svc-ai"
    profile: str = Field(default="local", alias="SPRING_PROFILES_ACTIVE")
    port: int = 8084

    jwt_secret: str = Field(
        default="change-me-in-production-must-be-at-least-32-bytes-long-xx",
        alias="SYNAPSE_JWT_SECRET",
    )
    kafka_bootstrap: str = Field(default="localhost:9092", alias="KAFKA_BOOTSTRAP")


@lru_cache
def get_settings() -> Settings:
    return Settings()
