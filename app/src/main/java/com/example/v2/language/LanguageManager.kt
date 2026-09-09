package com.example.v2.language

enum class LanguageMode {
    AUTO_DETECT,
    FIXED,
    MULTILINGUAL
}

data class LanguageConfig(
    val code: String,
    val name: String,
    val nativeName: String,
    val supported: Boolean = true
)

object LanguageManager {
    val supportedLanguages = listOf(
        LanguageConfig("en", "English", "English"),
        LanguageConfig("bn", "Bengali", "বাংলা"),
        LanguageConfig("hi", "Hindi", "हिन्दी"),
        LanguageConfig("ur", "Urdu", "اردو"),
        LanguageConfig("ta", "Tamil", "தமிழ்"),
        LanguageConfig("te", "Telugu", "తెలుగు"),
        LanguageConfig("kn", "Kannada", "ಕನ್ನಡ"),
        LanguageConfig("ml", "Malayalam", "മലയാളം"),
        LanguageConfig("mr", "Marathi", "मराठी"),
        LanguageConfig("gu", "Gujarati", "ગુજરાતી"),
        LanguageConfig("pa", "Punjabi", "ਪੰਜਾਬੀ"),
        LanguageConfig("es", "Spanish", "Español"),
        LanguageConfig("fr", "French", "Français"),
        LanguageConfig("de", "German", "Deutsch"),
        LanguageConfig("it", "Italian", "Italiano"),
        LanguageConfig("pt", "Portuguese", "Português"),
        LanguageConfig("ar", "Arabic", "العربية"),
        LanguageConfig("tr", "Turkish", "Türkçe"),
        LanguageConfig("ru", "Russian", "Русский"),
        LanguageConfig("ja", "Japanese", "日本語"),
        LanguageConfig("ko", "Korean", "한국어"),
        LanguageConfig("zh", "Chinese", "中文"),
        LanguageConfig("nl", "Dutch", "Nederlands"),
        LanguageConfig("id", "Indonesian", "Bahasa Indonesia"),
        LanguageConfig("vi", "Vietnamese", "Tiếng Việt")
    )

    fun getFallbackLanguage(): LanguageConfig {
        return supportedLanguages.first { it.code == "en" }
    }

    fun getLanguageByCode(code: String): LanguageConfig {
        return supportedLanguages.find { it.code == code } ?: getFallbackLanguage()
    }
}
