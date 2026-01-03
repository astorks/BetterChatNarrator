package mc.betterchatnarrator.mod.ui

import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.*
import mc.betterchatnarrator.mod.BetterChatNarrator
import mc.betterchatnarrator.mod.Configuration
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class AzureConfigScreen(previousScreen: Screen? = null) : BaseScreen(previousScreen) {

    private val txtAzureSpeechApiKey = Components.textBox(Sizing.fill(75), Configuration.instance.azureSpeechNarrator.apiKey ?: "")
    private val txtAzureSpeechRegion = Components.textBox(Sizing.fill(75), Configuration.instance.azureSpeechNarrator.region ?: "")


    override fun rebuild() {
        client?.setScreen(AzureConfigScreen(previousScreen))
    }

    override fun close() {
        Configuration.instance.azureSpeechNarrator.apiKey = txtAzureSpeechApiKey.text
        Configuration.instance.azureSpeechNarrator.region = txtAzureSpeechRegion.text
        Configuration.write()

        BetterChatNarrator.reloadNarrator()

        client?.setScreen(MenuScreen())
    }

    override fun build(rootComponent: FlowLayout) {
        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)

        rootComponent.child(
            Containers.verticalFlow(Sizing.fixed(300), Sizing.content())
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(Components.createWithSizing({ Components.label(Text.literal("Key")) }, Sizing.fill(20), Sizing.content()))
                        .child(txtAzureSpeechApiKey)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(5))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(Components.createWithSizing({ Components.label(Text.literal("Region")) }, Sizing.fill(20), Sizing.content()))
                        .child(txtAzureSpeechRegion)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(5))
                )
                .child(
                        Components.button(Text.literal("Close")) { _: ButtonComponent? -> close() }
                            .margins(Insets.top(10))
                )
                .padding(Insets.of(16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(false)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        )
    }
}