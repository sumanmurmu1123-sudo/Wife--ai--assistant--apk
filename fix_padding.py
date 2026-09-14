import re

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I want to make sure the microphone button looks centered and nice, and doesn't clip
# The size is 100.dp in MicrophoneButton. We might need to adjust the offset in FloatingNavBar

old_box = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .offset(y = (-32).dp),
            contentAlignment = Alignment.Center
        ) {"""

new_box = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .offset(y = (-40).dp),
            contentAlignment = Alignment.Center
        ) {"""

content = content.replace(old_box, new_box)

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'w', encoding='utf-8') as f:
    f.write(content)

