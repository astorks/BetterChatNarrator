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

class VoiceConfigScreen(previousScreen: Screen? = null) : BaseScreen(previousScreen) {

    private val sliderSpeechRate = Components.discreteSlider(Sizing.fill(55), 0.5, 2.0)
    private val sliderSpeechVolume = Components.discreteSlider(Sizing.fill(55), 0.0, 1.0)
    private val txtVoicePreview = Components.textBox(Sizing.fill(55), "This is an example message.")

    override fun rebuild() {
        Configuration.instance.azureSpeechNarrator.speechRate = sliderSpeechRate.discreteValue()
        Configuration.instance.azureSpeechNarrator.speechVolume = sliderSpeechVolume.discreteValue()
        Configuration.write()

        client?.setScreen(VoiceConfigScreen(previousScreen))
    }

    override fun close() {
        Configuration.instance.azureSpeechNarrator.speechRate = sliderSpeechRate.discreteValue()
        Configuration.instance.azureSpeechNarrator.speechVolume = sliderSpeechVolume.discreteValue()
        Configuration.write()

        client?.setScreen(MenuScreen())
    }

    override fun build(rootComponent: FlowLayout) {
        sliderSpeechRate.decimalPlaces(2)
        sliderSpeechVolume.decimalPlaces(2)
        txtVoicePreview.setMaxLength(500)

        sliderSpeechRate.setFromDiscreteValue(Configuration.instance.azureSpeechNarrator.speechRate)
        sliderSpeechVolume.setFromDiscreteValue(Configuration.instance.azureSpeechNarrator.speechVolume)

        sliderSpeechRate.slideEnd().subscribe {
            rebuild()
        }

        sliderSpeechVolume.slideEnd().subscribe {
            rebuild()
        }

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)

        rootComponent.child(
            Containers.verticalFlow(Sizing.fixed(300), Sizing.content())
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(Components.createWithSizing({ Components.label(Text.literal("Speech Rate: ${Configuration.instance.azureSpeechNarrator.speechRate.times(100)}%")) }, Sizing.fill(40), Sizing.content()))
                        .child(sliderSpeechRate)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(5))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(Components.createWithSizing({ Components.label(Text.literal("Volume: ${Configuration.instance.azureSpeechNarrator.speechVolume.times(100)}%")) }, Sizing.fill(40), Sizing.content()))
                        .child(sliderSpeechVolume)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(5))
                )
                .child(
                    Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
                        .child(Components.createWithSizing({ Components.label(Text.literal("Test Message")) }, Sizing.fill(40), Sizing.content()))
                        .child(txtVoicePreview)
                        .verticalAlignment(VerticalAlignment.CENTER)
                        .margins(Insets.bottom(5))
                )
                .child(
                    Containers.horizontalFlow(Sizing.content(), Sizing.content())
                        .child(
                            Components.button(Text.literal("Close")) {
                                close()
                            }
                        )
                        .child(
                            Components.button(Text.literal("Test Narrator")) {
                                BetterChatNarrator.testNarrator(txtVoicePreview.text)
                            }
                        )
                        .margins(Insets.top(16))
                )
                .padding(Insets.of(16))
                .surface(Surface.DARK_PANEL)
                .allowOverflow(false)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        )
    }
}