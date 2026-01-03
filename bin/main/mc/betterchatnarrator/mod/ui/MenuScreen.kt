package mc.betterchatnarrator.mod.ui

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import mc.betterchatnarrator.mod.AzureSpeechNarrator
import mc.betterchatnarrator.mod.BetterChatNarrator
import mc.betterchatnarrator.mod.Configuration
import mc.betterchatnarrator.shared.VoiceInfo
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.input.KeyInput
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class MenuScreen(previousScreen: Screen? = null) : BaseScreen(previousScreen) {
    override fun rebuild() {
        client?.setScreen(MenuScreen(previousScreen))
    }

    override fun keyPressed(input: KeyInput): Boolean {
        if (input.key == GLFW.GLFW_KEY_O && shouldCloseOnEsc()) {
            close()
            return true
        }

        return super.keyPressed(input)
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)

        val globalEnable = Configuration.instance.enabled
        val azureNarratorEnabled = globalEnable && Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR

        rootComponent.child(
            Containers.verticalFlow(Sizing.fixed(300), Sizing.content())
                .child(
                    Components.button(Text.literal(if (globalEnable) "§aBetter Narrator Enabled" else "§cBetter Narrator Disabled")) {
                        Configuration.instance.enabled = !Configuration.instance.enabled
                        Configuration.write()
                        rebuild()
                    }
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), if(globalEnable) Sizing.content() else Sizing.fixed(0))
                        .child(
                            Components.button(
                                Text.literal(if (azureNarratorEnabled) "§aEnhanced Narrator" else "§cBasic Narrator")) {
                                if(Configuration.instance.narratorType == Configuration.NarratorType.BASIC_GAME_NARRATOR) {
                                    Configuration.instance.narratorType = Configuration.NarratorType.AZURE_SPEECH_NARRATOR
                                } else {
                                    Configuration.instance.narratorType = Configuration.NarratorType.BASIC_GAME_NARRATOR
                                }
                                Configuration.write()
                                rebuild()
                            }
                        )
                        .child(
                            Components.button(
                                Text.literal(if(BetterChatNarrator.narrator is AzureSpeechNarrator) "§aConfig Valid" else "§cConfig Error")) {
                                client?.setScreen(AzureConfigScreen())
                            }
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(if(Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR) 5 else 0))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), if(azureNarratorEnabled) Sizing.content() else Sizing.fixed(0))
                        .child(
                            Components.button(Text.literal("Edit Speech Settings")) {
                                client?.setScreen(VoiceConfigScreen())
                            }
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(if(Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR) 5 else 0))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), if(azureNarratorEnabled) Sizing.content() else Sizing.fixed(0))
                        .child(
                            Components.button(Text.literal("Primary Voice: ${VoiceInfo.getVoiceNameById(Configuration.instance.azureSpeechNarrator.voice)}")) {
                                client?.setScreen(VoiceSelectScreen())
                            }
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(if(Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR) 5 else 0))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), if(azureNarratorEnabled) Sizing.content() else Sizing.fixed(0))
                        .child(
                            Components.button(Text.literal(if(Configuration.instance.azureSpeechNarrator.allowPlayerVoices) "§aPlayer Voices: Enabled" else "§cPlayer Voices: Disabled")) {
                                Configuration.instance.azureSpeechNarrator.allowPlayerVoices = !Configuration.instance.azureSpeechNarrator.allowPlayerVoices
                                Configuration.write()
                                rebuild()
                            }
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(if(Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR) 5 else 0))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), if(globalEnable) Sizing.content() else Sizing.fixed(0))
                        .child(
                            Components.button(Text.literal("Edit Active Channels")) {
                                client?.setScreen(ChannelConfigScreen())
                            }
                        )
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                        .margins(Insets.bottom(if(Configuration.instance.narratorType == Configuration.NarratorType.AZURE_SPEECH_NARRATOR) 5 else 0))
                )
                .child(
                        Components.button(Text.literal("Close")) { _: ButtonComponent? -> close() }
                            .margins(Insets.top(10))
                )
                .padding(Insets.of(16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(true)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        )
    }
}