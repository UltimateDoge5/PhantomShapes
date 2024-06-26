package org.pkozak

import dev.isxander.yacl3.api.NameableEnum
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder
import kotlinx.serialization.json.*
import net.minecraft.text.Text
import org.pkozak.util.SavedDataManager
import org.pkozak.util.SavedDataManager.Companion.toSafeBoolean
import java.util.*

class Options {
    var renderShapes = true
    val renderShapesOption: Option<Boolean> =
        Option.createBuilder<Boolean>() // boolean is the type of option we'll be making
            .name(Text.translatable("options.phantomshapes.enable_render"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.enable_render.tooltip")))
            .binding(true, { renderShapes }, { renderShapes = it })
            .controller { opt -> BooleanControllerBuilder.create(opt).coloured(true) }.build()

    var drawOnBlocks = false
    val drawOnBlocksOption: Option<Boolean> =
        Option.createBuilder<Boolean>() // boolean is the type of option we'll be making
            .name(Text.translatable("options.phantomshapes.draw_on_blocks"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.draw_on_blocks.tooltip")))
            .binding(false, { drawOnBlocks }, { drawOnBlocks = it })
            .controller { opt -> BooleanControllerBuilder.create(opt).coloured(true) }.build()

    var drawMode = DrawMode.BOTH
    val drawModeOption: Option<DrawMode> =
        Option.createBuilder<DrawMode>().name(Text.translatable("options.phantomshapes.draw_mode"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.draw_mode.tooltip")))
            .binding(DrawMode.BOTH, { drawMode }, { drawMode = it })
            .controller { opt -> EnumControllerBuilder.create(opt).enumClass(DrawMode::class.java) }.build()

    // Controls the face alpha
    var fillOpacity = 0.4f
    val fillOpacityOption: Option<Float> =
        Option.createBuilder<Float>().name(Text.translatable("options.phantomshapes.fill_opacity"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.fill_opacity.tooltip")))
            .binding(0.4f, { fillOpacity }, { fillOpacity = it }).controller { opt ->
                FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.1f).formatValue {
                    if (it == 0.0f) Text.of("Hidden") else Text.of((it * 100).toInt().toString() + "%")
                }
            }.build()

    // Controls the edge alpha
    var outlineOpacity = 0.7f
    val outlineOpacityOption: Option<Float> =
        Option.createBuilder<Float>().name(Text.translatable("options.phantomshapes.outline_opacity"))
            .description(OptionDescription.of(Text.translatable("options.phantomshapes.outline_opacity.tooltip")))
            .binding(0.7f, { outlineOpacity }, { outlineOpacity = it }).controller { opt ->
                FloatSliderControllerBuilder.create(opt).range(0.0f, 1.0f).step(0.1f).formatValue {
                    if (it == 0.0f) Text.of("Hidden") else Text.of((it * 100).toInt().toString() + "%")
                }
            }.build()

    // Try loading options from file, upon not finding a value, use the default as a fallback
    init {
        val jsonString = SavedDataManager.readFromFile("phantomshapes.json", true)
        if (jsonString != null) {
            val json = Json.decodeFromString(JsonObject.serializer(), jsonString)
            renderShapes = toSafeBoolean(json, "enableRender", renderShapes)
            drawOnBlocks = toSafeBoolean(json, "drawOnBlocks", drawOnBlocks)
            drawMode = try {
                DrawMode.valueOf(json["drawMode"]!!.jsonPrimitive.content.uppercase())
            } catch (e: IllegalArgumentException) {
                DrawMode.BOTH
            }
        }
    }

    fun saveToFile() {
        val json = buildJsonObject {
            put("enableRender", renderShapes)
            put("drawOnBlocks", drawOnBlocks)
            put("drawMode", drawMode.toString().lowercase())
        }
        SavedDataManager.writeToFile("phantomshapes.json", json.toString(), true)
    }

    enum class DrawMode : NameableEnum {
        EDGES, FACES, BOTH;

        override fun getDisplayName(): Text {
            return Text.translatable("options.phantomshapes.draw_mode." + name.lowercase(Locale.getDefault()))
        }
    }
}