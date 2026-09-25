import os
import uuid
import time
import json
import asyncio
import logging
from fastapi import FastAPI, Header, HTTPException, Request, WebSocket, WebSocketDisconnect
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import uvicorn
import google.generativeai as genai
from dotenv import load_dotenv
import websockets

load_dotenv()

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("MayaBackend")

app = FastAPI(title="Maya AI Backend", version="1.1.0")

# Configure Gemini
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "YOUR_API_KEY")
genai.configure(api_key=GEMINI_API_KEY)

# Session and Memory Store (In-memory for demo, should use DB/Redis)
sessions = {}
memories = {} # boss_name -> list of strings

class AuthRequest(BaseModel):
    boss_name: str

class AuthResponse(BaseModel):
    sessionId: str
    status: str
    config: Dict[str, Any]

class HealthResponse(BaseModel):
    status: str
    version: str

@app.get("/health", response_model=HealthResponse)
async def health_check():
    return {"status": "healthy", "version": "1.1.0"}

@app.post("/auth", response_model=AuthResponse)
async def authenticate(auth_req: AuthRequest):
    session_id = str(uuid.uuid4())
    boss_name = auth_req.boss_name
    
    sessions[session_id] = {
        "boss_name": boss_name,
        "created_at": int(time.time())
    }
    
    # Load memories for this boss
    boss_memories = memories.get(boss_name, [
        f"The assistant's boss is {boss_name}.",
        "The assistant is Maya, a caring AI partner."
    ])
    
    # Tool Registry (Simplified)
    tools = [
        {
            "name": "save_memory",
            "description": "Save a new piece of information about the user.",
            "parameters": {
                "type": "OBJECT",
                "properties": {
                    "fact": {"type": "STRING", "description": "The fact to remember."}
                },
                "required": ["fact"]
            }
        }
    ]
    
    config = {
        "system_instruction": f"Active Memories: {'; '.join(boss_memories)}",
        "tools": tools,
        "ephemeral_token": GEMINI_API_KEY # In a real Vertex setup, this would be a transient token
    }
    
    return {
        "sessionId": session_id,
        "status": "authenticated",
        "config": config
    }

@app.websocket("/ws/live")
async def websocket_gateway(websocket: WebSocket, session_id: Optional[str] = None):
    """
    Acts as a bridge between the Android App and Google Gemini Live API.
    """
    await websocket.accept()
    
    if not session_id or session_id not in sessions:
        logger.warning(f"Unauthorized WS attempt: {session_id}")
        await websocket.close(code=1008)
        return

    boss_name = sessions[session_id]["boss_name"]
    logger.info(f"Establishing Gemini Live bridge for {boss_name}")

    gemini_url = f"wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key={GEMINI_API_KEY}"

    try:
        async with websockets.connect(gemini_url) as gemini_ws:
            # Bidirectional proxying
            async def client_to_gemini():
                try:
                    while True:
                        message = await websocket.receive_text()
                        # Here the backend could intercept and modify messages
                        # e.g., logging usage, enforcing safety, or injecting context
                        await gemini_ws.send(message)
                except Exception as e:
                    logger.debug(f"Client to Gemini closed: {e}")

            async def gemini_to_client():
                try:
                    while True:
                        message = await gemini_ws.recv()
                        # Here the backend could parse tool calls
                        # and update its own memory store if 'save_memory' is called
                        data = json.loads(message)
                        if "serverContent" in data:
                             model_turn = data["serverContent"].get("modelTurn", {})
                             parts = model_turn.get("parts", [])
                             for part in parts:
                                 if "functionCall" in part:
                                     fc = part["functionCall"]
                                     if fc["name"] == "save_memory":
                                         fact = fc["args"].get("fact")
                                         if fact:
                                             if boss_name not in memories: memories[boss_name] = []
                                             memories[boss_name].append(fact)
                                             logger.info(f"Memory saved for {boss_name}: {fact}")
                        
                        await websocket.send_text(message)
                except Exception as e:
                    logger.debug(f"Gemini to Client closed: {e}")

            # Run both tasks concurrently
            await asyncio.gather(client_to_gemini(), gemini_to_client())

    except WebSocketDisconnect:
        logger.info(f"Client {session_id} disconnected.")
    except Exception as e:
        logger.error(f"Gateway Error: {e}")
        await websocket.close(code=1011)

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
