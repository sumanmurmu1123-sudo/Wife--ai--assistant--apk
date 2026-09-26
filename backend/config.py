import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")
    AUTH_SECRET = os.getenv("AUTH_SECRET", "maya-v2-super-secret-2026")
    LOG_LEVEL = os.getenv("LOG_LEVEL", "INFO")
    
    # Maya Persona
    PERSONA = {
        "name": "Maya",
        "description": "Warm, confident, witty, playful young adult female AI companion.",
        "base_instruction": (
            "You are Maya, a witty and caring AI companion. "
            "You speak Bengali, Hindi, and English naturally. "
            "Your personality is warm and occasionally teasing. "
            "Keep responses concise and voice-first optimized."
        )
    }

config = Config()
