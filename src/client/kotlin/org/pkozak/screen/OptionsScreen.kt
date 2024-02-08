package org.pkozak.screen

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.OptionListWidget
import net.minecraft.screen.ScreenTexts
import net.minecraft.text.Text
import org.pkozak.Options

class OptionsScreen(private val parent: Screen, private var options: Options) :
    Screen(Text.literal("PhantomShapes Options")) {
    private lateinit var list: OptionListWidget
    override fun init() {
        super.init()
        list = this.addDrawableChild(OptionListWidget(this.client, this.width, this.height - 64, 32, 25))

        list.addSingleOptionEntry(options.disableRender)
        list.addSingleOptionEntry(options.drawOnBlocks)
        list.addSingleOptionEntry(options.drawOnlyEdges)

        this.addDrawableChild(list)
        this.addDrawableChild(
            ButtonWidget.builder(
                ScreenTexts.DONE
            ) {
                // TODO: Save options to file
                client!!.setScreen(this.parent)
            }.dimensions(this.width / 2 - 100, this.height - 27, 200, 20).build()
        )
    }


    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF)
    }

    override fun close() {
        client?.setScreen(parent)
    }

}