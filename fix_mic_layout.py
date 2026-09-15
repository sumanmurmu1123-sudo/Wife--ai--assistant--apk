import re

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I want to make sure the microphone button looks centered and nice, and doesn't clip
old_box = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-16).dp), // Protrude from the bar
            contentAlignment = Alignment.Center
        ) {"""

new_box = """        // Center Talk Button (Floating above)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp), // Slight offset to float nicely
            contentAlignment = Alignment.Center
        ) {"""

content = content.replace(old_box, new_box)

with open('app/src/main/java/com/example/v2/ui/components/FloatingNavBar.kt', 'w', encoding='utf-8') as f:
    f.write(content)

