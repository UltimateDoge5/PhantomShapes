package org.pkozak

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

    // Try loading options from file
    init {
        val jsonString = SavedDataManager.readFromFile("writeToFile.json", true)
        if (jsonString != null) {
            val json = Json.decodeFromString(JsonObject.serializer(), jsonString)
            disableRender.value = json["disableRender"]!!.toString().toBoolean()
            drawOnBlocks.value = json["drawOnBlocks"]!!.toString().toBoolean()
            drawOnlyEdges.value = json["drawOnlyEdges"]!!.toString().toBoolean()
        }
    }

    fun saveToFile() {
        val json = buildJsonObject {
            put("disableRender", disableRender.value)
            put("drawOnBlocks", drawOnBlocks.value)
            put("drawOnlyEdges", drawOnlyEdges.value)
        }
        SavedDataManager.writeToFile("phantomshapes.json", json.toString(), true)
    }
}