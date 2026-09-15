import re

with open('app/src/main/java/com/example/hologram/GlowingHologramBubble.kt', 'r') as f:
    content = f.read()

# Add import
content = re.sub(r'import androidx\.compose\.ui\.unit\.dp', r'import androidx.compose.ui.unit.dp\nimport androidx.compose.ui.graphics.drawscope.withTransform', content)

# Fix withTransform call
content = content.replace(
    "androidx.compose.ui.graphics.drawscope.withTransform({", 
    "withTransform({"
)

with open('app/src/main/java/com/example/hologram/GlowingHologramBubble.kt', 'w') as f:
    f.write(content)
