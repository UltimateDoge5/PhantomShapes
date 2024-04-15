package org.pkozak

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import net.minecraft.text.Text
import org.pkozak.PhantomShapesClient.options


class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Options for Phantom Shapes"))
                .category(
                    ConfigCategory.createBuilder()
                        .name(Text.literal("Options"))
                        .group(
                            OptionGroup.createBuilder()
                                .name(Text.literal("General"))
                                .description(OptionDescription.of(Text.literal("General options for Phantom Shapes")))
                                .option(options.renderShapesOption)
                                .option(options.drawOnBlocksOption)
                                .option(options.drawModeOption)
                                .build()
                        )
                        .build()
                )
                .save { options.saveToFile() }
                .build()
                .generateScreen(parent)
        }
    }
}
