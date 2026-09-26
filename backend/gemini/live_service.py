import asyncio
import json
import logging
import websockets
from typing import AsyncGenerator, Optional
from backend.config import config

logger = logging.getLogger("GeminiLiveService")

class GeminiLiveService:
    def __init__(self):
        self.api_key = config.GEMINI_API_KEY
        self.url = f"wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key={self.api_key}"
        self.ws: Optional[websockets.WebSocketClientProtocol] = None

    async def connect(self):
        logger.info("Connecting to Gemini Live API...")
        try:
            self.ws = await websockets.connect(self.url)
            logger.info("Connected to Gemini Live API.")
        except Exception as e:
            logger.error(f"Failed to connect to Gemini: {e}")
            raise

    async def send(self, data: str):
        if self.ws:
            await self.ws.send(data)

    async def receive(self) -> AsyncGenerator[str, None]:
        if not self.ws:
            return
        try:
            async for message in self.ws:
                yield message
        except Exception as e:
            logger.error(f"Error receiving from Gemini: {e}")

    async def close(self):
        if self.ws:
            await self.ws.close()
            logger.info("Gemini Live session closed.")
