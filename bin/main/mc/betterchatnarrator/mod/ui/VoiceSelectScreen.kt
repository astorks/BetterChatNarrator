package mc.betterchatnarrator.mod.ui

import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import mc.betterchatnarrator.mod.BetterChatNarrator
import mc.betterchatnarrator.mod.Configuration
import mc.betterchatnarrator.shared.VoiceInfo
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class VoiceSelectScreen(previousScreen: Screen? = null) : BaseScreen(previousScreen) {
    private fun updatePrimaryVoice(voice: String) {
        Configuration.instance.azureSpeechNarrator.voice = voice
        BetterChatNarrator.updatePrimaryVoice(voice)
        Configuration.write()
        rebuild()
    }

    private fun previewVoice(voiceInfo: VoiceInfo) {
        BetterChatNarrator.testNarrator("Hello I'm ${voiceInfo.name}. This is an example message.", voiceInfo.id)
    }

    private fun voiceButtons(): Component {
        val dropdown = Components.dropdown(Sizing.fill(100))

        for (voice in VoiceInfo.allVoices) {
            dropdown.checkbox(Text.literal("${if(Configuration.instance.azureSpeechNarrator.voice == voice.id) "§a§l" else ""}${voice.color ?: ""}${voice.name}"), Configuration.instance.azureSpeechNarrator.voice == voice.id) {
                if(it) {
                    updatePrimaryVoice(voice.id)
                    previewVoice(voice)
                }
            }
        }

        return dropdown
    }

    override fun rebuild() {
        client?.setScreen(VoiceSelectScreen(previousScreen))
    }

    override fun close() {
        client?.setScreen(MenuScreen())
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)

        rootComponent.child(
            Containers.verticalFlow(Sizing.fixed(300), Sizing.fill(70))
                .child(Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), voiceButtons()))
                .child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(
                            Components.button(Text.literal("Close")) {
                                close()
                            }
                        )
                        .margins(Insets.top(16))
                )
                .padding(Insets.of(30, 30, 16, 16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(true)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        )
    }
}