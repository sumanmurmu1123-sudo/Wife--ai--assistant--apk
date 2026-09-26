from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_call(args: Dict[str, Any]) -> Dict[str, Any]:
    contact = args.get("contact", "Unknown")
    return {"status": "success", "message": f"Calling {contact}", "action": "CALL", "params": {"contact": contact}}

def register_phone_tools():
    registry.register(AssistantTool(
        id="phone.call",
        name="make_phone_call",
        description="Initiate a phone call to a contact or number.",
        category="PHONE",
        parameters={
            "type": "OBJECT",
            "properties": {
                "contact": {"type": "STRING", "description": "Name or number to call."}
            },
            "required": ["contact"]
        },
        execute_fn=handle_call
    ))
