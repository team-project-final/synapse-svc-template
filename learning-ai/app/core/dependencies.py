"""FastAPI 의존성 주입 — Spring의 @Autowired에 해당.

`Depends(get_settings)`로 헨들러에서 받아 사용.
"""

from typing import Annotated

from fastapi import Depends

from app.core.config import Settings, get_settings

SettingsDep = Annotated[Settings, Depends(get_settings)]
