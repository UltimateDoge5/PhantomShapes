package org.pkozak

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.client.option.SimpleOption
import net.minecraft.text.Text
import org.pkozak.util.SavedDataManager
import org.pkozak.util.SavedDataManager.Companion.toSafeBoolean

class Options {
    private val RENDERING_TOOLTIP: Text = Text.translatable("options.phantomshapes.enable_render.tooltip")
    var renderShapes: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.enable_render",
        SimpleOption.constantTooltip(RENDERING_TOOLTIP),
        true
    )

    private val DRAW_ON_BLOCKS_TOOLTIP: Text = Text.translatable("options.phantomshapes.draw_on_blocks.tooltip")
    var drawOnBlocks: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.draw_on_blocks",
        SimpleOption.constantTooltip(DRAW_ON_BLOCKS_TOOLTIP),
        false,
    )

    private val DRAW_ONLY_EDGES_TOOLTIP: Text = Text.translatable("options.phantomshapes.draw_only_edges.tooltip")
    val drawOnlyEdges: SimpleOption<Boolean> = SimpleOption.ofBoolean(
        "options.phantomshapes.draw_only_edges",
        SimpleOption.constantTooltip(DRAW_ONLY_EDGES_TOOLTIP),
        false
    )

    // Try loading options from file, upon not finding a value, use the default as a fallback
    init {
        val jsonString = SavedDataManager.readFromFile("phantomshapes.json", true)
        if (jsonString != null) {
            val json = Json.decodeFromString(JsonObject.serializer(), jsonString)
            renderShapes.value = toSafeBoolean(json, "enableRender", this.renderShapes.value)
            drawOnBlocks.value = toSafeBoolean(json, "drawOnBlocks", this.drawOnBlocks.value)
            drawOnlyEdges.value = toSafeBoolean(json, "drawOnlyEdges", this.drawOnlyEdges.value)
        }
    }

    fun saveToFile() {
        val json = buildJsonObject {
            put("enableRender", renderShapes.value)
            put("drawOnBlocks", drawOnBlocks.value)
            put("drawOnlyEdges", drawOnlyEdges.value)
        }
        SavedDataManager.writeToFile("phantomshapes.json", json.toString(), true)
    }
}