import os
import uuid
import logging
import asyncio
import json
import base64
from fastapi import FastAPI, WebSocket, WebSocketDisconnect, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, Optional

from backend.config import config
from backend.gemini.live_service import GeminiLiveService
from backend.tools.registry import registry
from backend.tools.system.base import register_system_tools
from backend.tools.phone.base import register_phone_tools
from backend.tools.media.base import register_media_tools
from backend.tools.web.base import register_web_tools
from backend.tools.pc.base import register_pc_tools
from backend.tools.automation.base import register_automation_tools

# Initialize tools
register_system_tools()
register_phone_tools()
register_media_tools()
register_web_tools()
register_pc_tools()
register_automation_tools()

# Setup Logging
logging.basicConfig(level=config.LOG_LEVEL)
logger = logging.getLogger("MayaBackend")

app = FastAPI(title="Maya V2 Gateway")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

sessions: Dict[str, dict] = {}

class AuthRequest(BaseModel):
    client_id: str

class AuthResponse(BaseModel):
    session_token: str
    status: str

@app.get("/health")
async def health():
    return {"status": "healthy", "version": "2.1.0", "tools_count": len(registry.tools)}

@app.post("/auth/session", response_model=AuthResponse)
async def create_session(request: AuthRequest):
    session_token = str(uuid.uuid4())
    sessions[session_token] = {"client_id": request.client_id}
    logger.info(f"Session created for client: {request.client_id}")
    return {"session_token": session_token, "status": "authenticated"}

@app.websocket("/ws/maya")
async def maya_ws_gateway(websocket: WebSocket, token: Optional[str] = None):
    if not token or token not in sessions:
        logger.warning(f"Unauthorized WS attempt: {token}")
        await websocket.close(code=1008)
        return

    await websocket.accept()
    logger.info(f"Maya V2 connected. Token: {token}")

    gemini_service = GeminiLiveService()
    try:
        await gemini_service.connect()
        
        async def client_to_gemini():
            try:
                while True:
                    data = await websocket.receive_text()
                    msg = json.loads(data)
                    event = msg.get("event")
                    
                    if event == "session.start":
                        setup = msg.get("setup", {})
                        logger.info("Injecting Maya Persona and Tools into setup...")
                        gemini_setup = {
                            "setup": {
                                "model": "models/gemini-2.0-flash",
                                "generationConfig": setup.get("generationConfig", {}),
                                "systemInstruction": {
                                    "parts": [{"text": config.PERSONA["base_instruction"]}]
                                },
                                "tools": [{"function_declarations": registry.get_all_gemini_tools()}]
                            }
                        }
                        await gemini_service.send(json.dumps(gemini_setup))
                    
                    elif event == "audio.input":
                        audio_data = msg.get("data")
                        if audio_data:
                            gemini_msg = {
                                "realtimeInput": {
                                    "mediaChunks": [{
                                        "mimeType": "audio/pcm;rate=16000",
                                        "data": audio_data
                                    }]
                                }
                            }
                            await gemini_service.send(json.dumps(gemini_msg))
                            
                    elif event == "user.interrupt":
                        logger.info("User interruption received.")
                        # Send empty client content to signal interruption to Gemini
                        gemini_msg = {
                            "clientContent": {
                                "turnComplete": True
                            }
                        }
                        await gemini_service.send(json.dumps(gemini_msg))
                        await websocket.send_text(json.dumps({"event": "interrupted"}))
                        
                    elif event == "ping":
                        await websocket.send_text(json.dumps({"event": "pong"}))
                        
            except Exception as e:
                logger.debug(f"Client stream closed: {e}")

        async def gemini_to_client():
            try:
                async for message in gemini_service.receive():
                    gemini_msg = json.loads(message)
                    
                    # Handle Gemini Setup Complete
                    if "setupComplete" in gemini_msg:
                        await websocket.send_text(json.dumps({"event": "session.ready"}))
                        continue

                    # Handle Server Content (Audio/Text/Tools)
                    if "serverContent" in gemini_msg:
                        content = gemini_msg["serverContent"]
                        
                        if content.get("interrupted"):
                            await websocket.send_text(json.dumps({"event": "interrupted"}))
                        
                        if content.get("turnComplete"):
                            await websocket.send_text(json.dumps({"event": "turn.completed"}))

                        if "modelTurn" in content:
                            parts = content["modelTurn"].get("parts", [])
                            for part in parts:
                                if "inlineData" in part:
                                    audio_b64 = part["inlineData"].get("data")
                                    if audio_b64:
                                        await websocket.send_text(json.dumps({
                                            "event": "audio.output",
                                            "data": audio_b64
                                        }))
                                
                                if "functionCall" in part:
                                    fc = part["functionCall"]
                                    name = fc["name"]
                                    args = fc.get("args", {})
                                    logger.info(f"Executing server-side tool: {name}")
                                    result = await registry.execute(name, args)
                                    # Forward tool result back to Gemini
                                    tool_resp = {
                                        "toolResponse": {
                                            "functionResponses": [{
                                                "name": name,
                                                "response": result
                                            }]
                                        }
                                    }
                                    await gemini_service.send(json.dumps(tool_resp))
                                    # Also notify Android client
                                    await websocket.send_text(json.dumps({
                                        "event": "tool.call",
                                        "name": name,
                                        "args": args,
                                        "result": result
                                    }))

                    # Handle Tool Call from Model (if not in modelTurn, though Gemini usually puts it there)
                    if "toolCall" in gemini_msg:
                        # Similar logic if needed
                        pass

            except Exception as e:
                logger.debug(f"Gemini stream closed: {e}")

        await asyncio.gather(client_to_gemini(), gemini_to_client())

    except WebSocketDisconnect:
        logger.info("Maya V2 App disconnected.")
    except Exception as e:
        logger.error(f"Gateway error: {e}")
    finally:
        await gemini_service.close()
        if token in sessions:
            del sessions[token]

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
