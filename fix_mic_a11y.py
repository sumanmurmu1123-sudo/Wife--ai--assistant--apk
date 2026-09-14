import re

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'r', encoding='utf-8') as f:
    content = f.read()

reps = {
    'contentDescription = "Permission Required"': 'contentDescription = "মাইক্রোফোন অনুমতি দিন"',
    'contentDescription = "Disconnected"': 'contentDescription = "ভয়েস সংযোগ বিচ্ছিন্ন"',
    'contentDescription = "Connecting"': 'contentDescription = "ভয়েস সংযোগ হচ্ছে"',
    'contentDescription = "Connected"': 'contentDescription = "ভয়েস চালু করুন"',
    'contentDescription = "Listening"': 'contentDescription = "শুনছি, থামাতে চাপুন"',
    'contentDescription = "Thinking"': 'contentDescription = "ভাবছি"',
    'contentDescription = "Speaking"': 'contentDescription = "কথা চলছে"',
    'contentDescription = "Error"': 'contentDescription = "ভয়েস ত্রুটি, পুনরায় চেষ্টা করুন"'
}

for k, v in reps.items():
    content = content.replace(k, v)

with open('app/src/main/java/com/example/v2/ui/components/MicrophoneButton.kt', 'w', encoding='utf-8') as f:
    f.write(content)
