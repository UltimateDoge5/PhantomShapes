package org.pkozak

import com.mojang.serialization.Codec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.client.option.SimpleOption
import net.minecraft.text.Text
import net.minecraft.util.TranslatableOption
import net.minecraft.util.function.ValueLists
import org.pkozak.util.SavedDataManager
import org.pkozak.util.SavedDataManager.Companion.toSafeBoolean
import org.pkozak.util.SavedDataManager.Companion.toSafeInt
import java.util.function.IntFunction

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

    private val DRAW_MODE_TOOLTIP: Text = Text.translatable("options.phantomshapes.draw_mode.tooltip")
    val drawMode: SimpleOption<DrawMode> =
        SimpleOption("options.phantomshapes.draw_mode",
            SimpleOption.constantTooltip(DRAW_MODE_TOOLTIP),
            SimpleOption.enumValueText(),
            SimpleOption.PotentialValuesBasedCallbacks(
                listOf(*DrawMode.entries.toTypedArray()), Codec.INT.xmap(
                    { id: Int -> DrawMode.byId(id) }, { obj: DrawMode -> obj.id }
                )
            ),
            DrawMode.BOTH
        ) { }

    // Try loading options from file, upon not finding a value, use the default as a fallback
    init {
        val jsonString = SavedDataManager.readFromFile("phantomshapes.json", true)
        if (jsonString != null) {
            val json = Json.decodeFromString(JsonObject.serializer(), jsonString)
            renderShapes.value = toSafeBoolean(json, "enableRender", this.renderShapes.value)
            drawOnBlocks.value = toSafeBoolean(json, "drawOnBlocks", this.drawOnBlocks.value)
            drawMode.value = DrawMode.byId(toSafeInt(json, "drawMode", this.drawMode.value.id))
        }
    }

    fun saveToFile() {
        val json = buildJsonObject {
            put("enableRender", renderShapes.value)
            put("drawOnBlocks", drawOnBlocks.value)
            put("drawMode", drawMode.value.id)
        }
        SavedDataManager.writeToFile("phantomshapes.json", json.toString(), true)
    }

    enum class DrawMode(
        private val id: Int,
        private val translationKey: String
    ) : TranslatableOption {
        EDGES(0, "options.phantomshapes.draw_mode.edges"),
        FACES(1, "options.phantomshapes.draw_mode.faces"),
        BOTH(2, "options.phantomshapes.draw_mode.both");

        override fun getId(): Int {
            return this.id
        }

        override fun getTranslationKey(): String {
            return this.translationKey
        }

        companion object {
            private val BY_ID: IntFunction<DrawMode> = ValueLists.createIdToValueFunction(
                { obj: DrawMode -> obj.id },
                entries.toTypedArray(),
                ValueLists.OutOfBoundsHandling.WRAP
            )

            fun byId(id: Int): DrawMode {
                return BY_ID.apply(id)
            }
        }
    }
}