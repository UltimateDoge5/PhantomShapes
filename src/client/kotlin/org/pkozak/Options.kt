package org.pkozak

import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.text.Text
import org.pkozak.util.SavedDataManager
import org.pkozak.util.SavedDataManager.Companion.toSafeBoolean
import org.pkozak.util.SavedDataManager.Companion.toSafeFloat

class Options {
    var renderShapes = true
    var renderShapesOption: Option<Boolean>? = null

    var drawOnBlocks = false
    var drawOnBlocksOption: Option<Boolean>? = null

    var drawMode = DrawMode.BOTH
    var drawModeOption: Option<DrawMode>? = null

    // Controls the face alpha
    var fillOpacity = 0.4f
    var fillOpacityOption: Option<Float>?

    // Controls the edge alpha
    var outlineOpacity = 0.7f
    var outlineOpacityOption: Option<Float>? = null

    // Controls the size of the phantom blocks
    var blockSize = 1f
    var blockSizeOption: Option<Float>? = null

    // Try loading options from file, upon not finding a value, use the default as a fallback
    init {
        val jsonString = SavedDataManager.readFromFile("phantomshapes.json", true)
        if (jsonString != null) {
            val json = Json.decodeFromString(JsonObject.serializer(), jsonString)
            renderShapes = toSafeBoolean(json, "enableRender", renderShapes)
            drawOnBlocks = toSafeBoolean(json, "drawOnBlocks", drawOnBlocks)
            drawMode = when (json["drawMode"].toString().replace("\"", "")) {
                "edges" -> DrawMode.EDGES
                "faces" -> DrawMode.FACES
                else -> DrawMode.BOTH
            }
            fillOpacity = toSafeFloat(json, "fillOpacity", fillOpacity)
            outlineOpacity = toSafeFloat(json, "outlineOpacity", outlineOpacity)
            blockSize = toSafeFloat(json, "blockSize", blockSize)
        }

        renderShapesOption = Option.createBuilder<Boolean>()
            .name(Text.translatable("options.phantomshapes.enable_render"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.enable_render.tooltip")))
            .binding(true, { renderShapes }, { renderShapes = it })
            .controller { opt -> BooleanControllerBuilder.create(opt).coloured(true) }.build()

        drawOnBlocksOption = Option.createBuilder<Boolean>()
            .name(Text.translatable("options.phantomshapes.draw_on_blocks"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.draw_on_blocks.tooltip")))
            .binding(false, { drawOnBlocks }, { drawOnBlocks = it })
            .controller { opt -> BooleanControllerBuilder.create(opt).coloured(true) }.build()

        drawModeOption = Option.createBuilder<DrawMode>().name(Text.translatable("options.phantomshapes.draw_mode"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.draw_mode.tooltip")))
            .binding(DrawMode.BOTH, { drawMode }, { drawMode = it })
            .controller { opt -> EnumControllerBuilder.create(opt).enumClass(DrawMode::class.java) }.build()

        fillOpacityOption = Option.createBuilder<Float>().name(Text.translatable("options.phantomshapes.fill_opacity"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.fill_opacity.tooltip")))
            .binding(0.4f, { fillOpacity }, { fillOpacity = it }).controller { opt ->
                FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.1f).formatValue {
                    if (it == 0.0f) Text.of("Hidden") else Text.of((it * 100).toInt().toString() + "%")
                }
            }.build()

        outlineOpacityOption =
            Option.createBuilder<Float>().name(Text.translatable("options.phantomshapes.outline_opacity"))
                .description(OptionDescription.of(Text.translatable("options.phantomshapes.outline_opacity.tooltip")))
                .binding(0.7f, { outlineOpacity }, { outlineOpacity = it }).controller { opt ->
                    FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.1f).formatValue {
                        if (it == 0.0f) Text.of("Hidden") else Text.of((it * 100).toInt().toString() + "%")
                    }
                }.build()

        blockSizeOption = Option.createBuilder<Float>().name(Text.translatable("options.phantomshapes.block_size"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.block_size.tooltip")))
            .binding(0.7f, { blockSize }, { blockSize = it }).controller { opt ->
                FloatSliderControllerBuilder.create(opt).range(0.1f, 1.0f).step(0.1f).formatValue {
                    Text.of((it * 100).toInt().toString() + "% of a block")
                }
            }.build()
    }

    fun saveToFile() {
        val json = buildJsonObject {
            put("enableRender", renderShapes)
            put("drawOnBlocks", drawOnBlocks)
            put("drawMode", drawMode.toString().lowercase())
            put("fillOpacity", fillOpacity)
            put("outlineOpacity", outlineOpacity)
            put("blockSize", blockSize)
        }
        SavedDataManager.writeToFile("phantomshapes.json", json.toString(), true)
    }

    enum class DrawMode {
        EDGES, FACES, BOTH;
    }
}