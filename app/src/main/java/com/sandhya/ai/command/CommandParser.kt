package com.sandhya.ai.command

enum class CommandType {
    APP_OPEN, APP_SEARCH, APP_ACTION, SYSTEM_ACTION,
    ACCESSIBILITY_ACTION, VOICE_REPLY, SCREEN_CAPTURE, CAMERA, CALL, NOTIFICATION, GENERAL_AI
}

data class AssistantCommand(
    val type: CommandType,
    val target: String? = null,
    val args: Map<String, String> = emptyMap()
)

class CommandParser {
    private val aliases = mapOf(
        "youtube" to "youtube", "यूट्यूब" to "youtube",
        "whatsapp" to "whatsapp", "व्हाट्सऐप" to "whatsapp",
        "instagram" to "instagram", "इंस्टाग्राम" to "instagram",
        "facebook" to "facebook", "telegram" to "telegram",
        "maps" to "maps", "map" to "maps", "गूगल मैप्स" to "maps",
        "gmail" to "gmail", "photos" to "photos", "drive" to "drive",
        "play store" to "play_store", "playstore" to "play_store",
        "camera" to "camera", "कैमरा" to "camera",
        "calculator" to "calculator", "clock" to "clock",
        "contacts" to "contacts", "phone" to "phone",
        "settings" to "settings", "सेटिंग्स" to "settings", "सेटिंग" to "settings"
    )

    fun parse(text: String): AssistantCommand {
        val s = text.trim().lowercase()
        val target = aliases.entries.firstOrNull { (alias, _) -> s.contains(alias) }?.value

        if (target != null && listOf("khol", "kholo", "open", "launch", "chala", "चलाओ", "खोलो", "खोल").any(s::contains)) {
            return AssistantCommand(CommandType.APP_OPEN, target)
        }

        if (s.contains("home") || s.contains("होम")) return AssistantCommand(CommandType.ACCESSIBILITY_ACTION, "home")
        if (s == "back" || s.contains("पीछे")) return AssistantCommand(CommandType.ACCESSIBILITY_ACTION, "back")
        if (s.contains("recents") || s.contains("recent apps")) return AssistantCommand(CommandType.ACCESSIBILITY_ACTION, "recents")
        if (s.contains("scroll down") || s.contains("नीचे scroll")) return AssistantCommand(CommandType.ACCESSIBILITY_ACTION, "scroll_down")
        if (s.contains("scroll up") || s.contains("ऊपर scroll")) return AssistantCommand(CommandType.ACCESSIBILITY_ACTION, "scroll_up")

        return AssistantCommand(CommandType.GENERAL_AI, args = mapOf("text" to text))
    }
}
