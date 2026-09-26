from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_automation(args: Dict[str, Any]) -> Dict[str, Any]:
    task = args.get("task", "")
    return {"status": "success", "message": f"Running automation for {task}", "action": "AUTOMATION", "params": {"task": task}}

def register_automation_tools():
    registry.register(AssistantTool(
        id="automation.run",
        name="run_automation",
        description="Automate repetitive tasks on the device.",
        category="AUTOMATION",
        parameters={
            "type": "OBJECT",
            "properties": {
                "task": {"type": "STRING", "description": "Description of the task to automate."}
            },
            "required": ["task"]
        },
        execute_fn=handle_automation
    ))
