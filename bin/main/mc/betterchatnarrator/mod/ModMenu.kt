package mc.betterchatnarrator.mod

import mc.betterchatnarrator.mod.ui.MenuScreen
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen

class ModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<MenuScreen> {
        return ConfigScreenFactory { previousScreen: Screen? -> MenuScreen(previousScreen) }
    }
}