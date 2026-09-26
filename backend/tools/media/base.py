from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_music(args: Dict[str, Any]) -> Dict[str, Any]:
    query = args.get("query", "")
    action = args.get("action", "play")
    return {"status": "success", "message": f"{action.capitalize()}ing {query}", "action": "MEDIA", "params": {"query": query, "media_action": action}}

def register_media_tools():
    registry.register(AssistantTool(
        id="media.music",
        name="control_music",
        description="Play, pause, or skip music.",
        category="MEDIA",
        parameters={
            "type": "OBJECT",
            "properties": {
                "query": {"type": "STRING", "description": "Song, artist, or playlist name."},
                "action": {"type": "STRING", "enum": ["play", "pause", "skip", "previous"], "description": "Action to perform."}
            },
            "required": ["action"]
        },
        execute_fn=handle_music
    ))
