import os
import uuid
import time
from fastapi import FastAPI, Header, HTTPException, Request
from pydantic import BaseModel
from typing import Optional
import uvicorn
import google.generativeai as genai
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Wife AI Backend", version="1.0.0")

# Configure Gemini
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "YOUR_API_KEY")
genai.configure(api_key=GEMINI_API_KEY)

# Simple session storage (in-memory)
# In production, use Redis or a database
sessions = {}

class AuthRequest(BaseModel):
    boss_name: str

class AuthResponse(BaseModel):
    sessionId: str
    status: str

class TokenResponse(BaseModel):
    ephemeralToken: str
    expiresAt: int

class HealthResponse(BaseModel):
    status: str
    version: str

@app.get("/health", response_model=HealthResponse)
async def health_check():
    return {"status": "healthy", "version": "1.0.0"}

@app.post("/auth", response_model=AuthResponse)
async def authenticate(auth_req: AuthRequest):
    session_id = str(uuid.uuid4())
    sessions[session_id] = {
        "boss_name": auth_req.boss_name,
        "created_at": int(time.time())
    }
    return {"sessionId": session_id, "status": "authenticated"}

@app.get("/live/token", response_model=TokenResponse)
async def get_live_token(x_session_id: Optional[str] = Header(None)):
    if not x_session_id or x_session_id not in sessions:
        raise HTTPException(status_code=401, detail="Invalid session")
    
    # For Gemini AI Studio keys, we typically pass the key itself if the client expects a token
    # or the client uses the key directly. 
    # If the Android app uses 'tokenOverride' as a Bearer token, we provide the API key here
    # provided the client logic handles it (which it does in GeminiLiveManager.kt).
    
    return {
        "ephemeralToken": GEMINI_API_KEY, 
        "expiresAt": int(time.time()) + 3600
    }

# Additional endpoint for regular chat if needed
class ChatRequest(BaseModel):
    message: str
    history: Optional[list] = []

@app.post("/chat")
async def chat(req: ChatRequest, x_session_id: Optional[str] = Header(None)):
    if not x_session_id or x_session_id not in sessions:
        raise HTTPException(status_code=401, detail="Invalid session")
    
    try:
        model = genai.GenerativeModel('gemini-1.5-flash')
        # Simple chat implementation
        chat_session = model.start_chat(history=[])
        response = chat_session.send_message(req.message)
        return {"response": response.text}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
