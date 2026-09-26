from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_pc_command(args: Dict[str, Any]) -> Dict[str, Any]:
    command = args.get("command", "")
    return {"status": "success", "message": f"Executing {command} on PC", "action": "PC_CONTROL", "params": {"command": command}}

def register_pc_tools():
    registry.register(AssistantTool(
        id="pc.control",
        name="control_pc",
        description="Control the user's computer (requires desktop agent).",
        category="PC",
        parameters={
            "type": "OBJECT",
            "properties": {
                "command": {"type": "STRING", "description": "Command or app to launch."}
            },
            "required": ["command"]
        },
        execute_fn=handle_pc_command
    ))
