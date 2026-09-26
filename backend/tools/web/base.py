from typing import Dict, Any
from backend.tools.registry import AssistantTool, registry

async def handle_web_search(args: Dict[str, Any]) -> Dict[str, Any]:
    query = args.get("query", "")
    return {"status": "success", "message": f"Searching the web for {query}", "action": "WEB_SEARCH", "params": {"query": query}}

def register_web_tools():
    registry.register(AssistantTool(
        id="web.search",
        name="web_search",
        description="Search the web for information.",
        category="WEB",
        parameters={
            "type": "OBJECT",
            "properties": {
                "query": {"type": "STRING", "description": "Search query."}
            },
            "required": ["query"]
        },
        execute_fn=handle_web_search
    ))
