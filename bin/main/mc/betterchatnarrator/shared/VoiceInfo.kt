package mc.betterchatnarrator.shared

data class VoiceInfo(val id: String, val name: String, val color: String? = null) {
    companion object {
        val maleVoices = listOf(
            VoiceInfo("en-US-DavisNeural", "Davis", "§l* "),
            VoiceInfo("en-US-GuyNeural", "Guy", "§l* "),
            VoiceInfo("en-US-TonyNeural", "Tony", "§l* "),
            VoiceInfo("en-US-JasonNeural", "Jason", "§l* "),
        )

        val femaleVoices = listOf(
            VoiceInfo("en-US-JennyNeural", "Jenny", "§l* "),
            VoiceInfo("en-US-AriaNeural", "Aria", "§l* "),
            VoiceInfo("en-US-SaraNeural", "Sara", "§l* "),
            VoiceInfo("en-US-NancyNeural", "Nancy", "§l* "),
            VoiceInfo("en-US-JaneNeural", "Jane", "§l* "),
        )

        val basicVoices = listOf(
            VoiceInfo("en-US-RogerNeural", "Roger"),
            VoiceInfo("en-US-SteffanNeural", "Steffan"),
            VoiceInfo("en-US-BrandonNeural", "Brandon"),
            VoiceInfo("en-US-ChristopherNeural", "Christopher"),
            VoiceInfo("en-US-JacobNeural", "Jacob"),
            VoiceInfo("en-US-CoraNeural", "Cora"),
            VoiceInfo("en-US-AmberNeural", "Amber"),
            VoiceInfo("en-US-MichelleNeural", "Michelle"),
            VoiceInfo("en-US-AshleyNeural", "Ashley"),
            VoiceInfo("en-US-ElizabethNeural", "Elizabeth"),
        )

        val allVoices: List<VoiceInfo>
            get() = maleVoices + femaleVoices + basicVoices

        fun getVoiceInfoById(voiceId: String): VoiceInfo? {
            return allVoices.firstOrNull { it.id == voiceId }
        }

        fun getVoiceNameById(voiceId: String): String? {
            return allVoices.firstOrNull { it.id == voiceId }?.name
        }
    }
}