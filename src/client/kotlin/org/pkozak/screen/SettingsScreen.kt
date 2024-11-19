package org.pkozak.screen

import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.pkozak.PhantomShapesClient
import org.pkozak.PhantomShapesClient.options

class SettingsScreen {
    companion object {
        fun generateScreen(parent: Screen): Screen {
            return YetAnotherConfigLib.createBuilder()
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
                                .build()
                        )
                        .group(
                            OptionGroup.createBuilder()
                                .name(Text.literal("Visuals"))
                                .description(OptionDescription.of(Text.literal("Change the appearance of the phantom blocks")))
                                .option(options.drawModeOption)
                                .option(options.fillOpacityOption)
                                .option(options.outlineOpacityOption)
                                .build()
                        )
                        .build()
                )

                .save {
                    PhantomShapesClient.rerenderAllShapes()
                    options.saveToFile()
                }
                .build()
                .generateScreen(parent)
        }
    }
}