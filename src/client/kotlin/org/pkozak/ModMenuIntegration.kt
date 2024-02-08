package org.pkozak

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen
import org.pkozak.screen.OptionsScreen
import org.pkozak.screen.ShapesScreen


class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> { parent ->
            OptionsScreen(parent, PhantomShapesClient.options)
        }
    }
}
