package mc.betterchatnarrator.mod.ui

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import mc.betterchatnarrator.mod.AzureSpeechNarrator
import mc.betterchatnarrator.mod.BetterChatNarrator
import mc.betterchatnarrator.mod.Configuration
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class ChannelConfigScreen(previousScreen: Screen? = null) : BaseScreen(previousScreen) {

    override fun rebuild() {
        client?.setScreen(ChannelConfigScreen(previousScreen))
    }

    override fun close() {
        client?.setScreen(MenuScreen())
    }

    private fun channelDropdown(): Component {
        val dropdown = Components.dropdown(Sizing.fill(100))

        for (narratorChannel in BetterChatNarrator.narratorChannels) {
            val isDisabled = Configuration.instance.disabledNarratorChannels.contains(narratorChannel.key)

            val textName = narratorChannel.value.name
            dropdown.checkbox(Text.literal("${if(!isDisabled) "§a" else "§c"}${textName}"), !isDisabled) {
                if(it) {
                    Configuration.instance.disabledNarratorChannels.remove(narratorChannel.key)
                    BetterChatNarrator.testNarrator("$textName is enabled.")
                } else {
                    Configuration.instance.disabledNarratorChannels.add(narratorChannel.key)
                    BetterChatNarrator.testNarrator("$textName is disabled.")
                }

                rebuild()
            }
        }

        return dropdown
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)

        rootComponent.child(
            Containers.verticalFlow(Sizing.fixed(300), Sizing.fill(70))
                .child(Containers.verticalScroll(Sizing.fill(100), Sizing.fill(100), channelDropdown()))
                .child(
                        Components.button(Text.literal("Close")) { _: ButtonComponent? -> close() }
                            .margins(Insets.top(10))
                )
                .padding(Insets.of(30, 30, 16, 16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(true)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        )
    }
}