from typing import Dict, Any, List, Callable, Optional
import json

class AssistantTool:
    def __init__(self, id: str, name: str, description: str, category: str, parameters: Dict[str, Any], execute_fn: Callable):
        self.id = id
        self.name = name
        self.description = description
        self.category = category
        self.parameters = parameters
        self.execute_fn = execute_fn

    def to_gemini_format(self) -> Dict[str, Any]:
        return {
            "name": self.name,
            "description": self.description,
            "parameters": self.parameters
        }

class ToolRegistry:
    def __init__(self):
        self.tools: Dict[str, AssistantTool] = {}

    def register(self, tool: AssistantTool):
        self.tools[tool.name] = tool

    def get_all_gemini_tools(self) -> List[Dict[str, Any]]:
        return [t.to_gemini_format() for t in self.tools.values()]

    async def execute(self, name: str, args: Dict[str, Any]) -> Any:
        if name in self.tools:
            return await self.tools[name].execute_fn(args)
        return {"error": f"Tool {name} not found"}

registry = ToolRegistry()
