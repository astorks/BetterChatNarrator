package mc.betterchatnarrator.mod.ui

import io.wispforest.owo.ui.base.BaseOwoScreen
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIAdapter
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.gui.screen.Screen
import org.lwjgl.glfw.GLFW

abstract class BaseScreen(val previousScreen: Screen? = null) : BaseOwoScreen<FlowLayout>() {
    override fun createAdapter(): OwoUIAdapter<FlowLayout> {
        return OwoUIAdapter.create(this) { horizontalSizing: Sizing, verticalSizing: Sizing ->
            Containers.verticalFlow(horizontalSizing, verticalSizing)
        }
    }

    override fun close() {
        if (client != null) {
            client!!.setScreen(previousScreen)
        }
    }

    abstract fun rebuild()
}