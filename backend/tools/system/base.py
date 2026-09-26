from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_flashlight(args: Dict[str, Any]) -> Dict[str, Any]:
    enabled = args.get("enabled", False)
    return {"status": "success", "message": f"Flashlight {'on' if enabled else 'off'}", "action": "FLASHLIGHT", "params": {"enabled": enabled}}

async def handle_volume(args: Dict[str, Any]) -> Dict[str, Any]:
    level = args.get("level", 50)
    return {"status": "success", "message": f"Volume set to {level}", "action": "VOLUME", "params": {"level": level}}

def register_system_tools():
    registry.register(AssistantTool(
        id="system.flashlight",
        name="toggle_flashlight",
        description="Turn the phone flashlight on or off.",
        category="SYSTEM_CONTROL",
        parameters={
            "type": "OBJECT",
            "properties": {
                "enabled": {"type": "BOOLEAN", "description": "True to turn on, False to turn off."}
            },
            "required": ["enabled"]
        },
        execute_fn=handle_flashlight
    ))
    
    registry.register(AssistantTool(
        id="system.volume",
        name="set_volume",
        description="Set the phone volume level.",
        category="SYSTEM_CONTROL",
        parameters={
            "type": "OBJECT",
            "properties": {
                "level": {"type": "INTEGER", "description": "Volume level from 0 to 100."}
            },
            "required": ["level"]
        },
        execute_fn=handle_volume
    ))
