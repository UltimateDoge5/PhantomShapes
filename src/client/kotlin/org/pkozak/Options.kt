package org.pkozak

import net.minecraft.client.option.SimpleOption
import net.minecraft.text.Text

class Options {
    private val DISABLE_RENDERING_TOOLTIP: Text = Text.translatable("options.phantomshapes.disable_render.tooltip")
    var disableRender: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.disable_render",
        SimpleOption.constantTooltip(DISABLE_RENDERING_TOOLTIP),
        false
    )

    private val DRAW_ON_BLOCKS_TOOLTIP: Text = Text.translatable("options.phantomshapes.draw_on_blocks.tooltip")
    var drawOnBlocks: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.draw_on_blocks",
        SimpleOption.constantTooltip(DRAW_ON_BLOCKS_TOOLTIP),
        true
    )

    private val DRAW_ONLY_EDGES_TOOLTIP: Text = Text.translatable("options.phantomshapes.draw_only_edges.tooltip")
    val drawOnlyEdges: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.draw_only_edges",
        SimpleOption.constantTooltip(DRAW_ONLY_EDGES_TOOLTIP),
        false
    )
}