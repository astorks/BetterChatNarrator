package mc.betterchatnarrator.shared

import kotlinx.serialization.Serializable

@Serializable
data class NarratorConfig (
    val enabled: Boolean = true,
    val disabledNarratorChannels: MutableList<String> = mutableListOf(),
    val azureSpeechNarrator: AzureSpeechNarratorConfig = AzureSpeechNarratorConfig(),
    val narratorType: NarratorType = NarratorType.BASIC_GAME_NARRATOR
) {
    @Serializable
    data class AzureSpeechNarratorConfig(
        var apiKey: String? = null,
        var region: String? = null,
        var voice: String = "en-US-GuyNeural",
        var allowPlayerVoices: Boolean = true,
        var speechRate: Double = 1.0,
        var speechVolume: Double = 1.0
    )

    enum class NarratorType {
        BASIC_GAME_NARRATOR,
        AZURE_SPEECH_NARRATOR
    }
}