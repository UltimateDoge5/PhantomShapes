package org.pkozak

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.pkozak.screen.ShapesScreen
import org.pkozak.shape.Shape
import org.pkozak.util.RenderUtil
import org.pkozak.util.SavedDataManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object PhantomShapesClient : ClientModInitializer {
    val logger: Logger = LoggerFactory.getLogger("phantomshapes")
    val client: MinecraftClient = MinecraftClient.getInstance()
    val options = Options()

    var overwriteProtection = false

    private var shapes = mutableListOf<Shape>()
    private var quadVboMap = mutableMapOf<String, VertexBuffer>()
    private var outlineVboMap = mutableMapOf<String, VertexBuffer>()

    private var menuKeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.phantomshapes.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.phantomshapes.controls"
        )
    )

    private val toggleRenderKeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.phantomshapes.toggle_render",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.phantomshapes.controls"
        )
    )

    private var rerenderShapesKeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.phantomshapes.rerender_shapes",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.phantomshapes.controls"
        )
    )

    override fun onInitializeClient() {
        WorldRenderEvents.END.register { context ->
            if (!options.renderShapes) return@register

            RenderSystem.enableBlend()
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            RenderSystem.depthFunc(GL11.GL_LESS)
            RenderSystem.setShader(GameRenderer::getPositionColorProgram)

            for (shape in shapes) {
                val distance = shape.pos.distanceTo(context.camera().pos)
                val renderDistance = MinecraftClient.getInstance().options.viewDistance.value * 16

                // Skip rendering if the shape is disabled or too far away
                if (!shape.enabled || distance > renderDistance) continue
                renderShape(context, shape)
            }

            RenderSystem.disableBlend()
            RenderSystem.depthMask(false)
            RenderSystem.enableCull()
        }

        // Listen for block break events to update rendered shape blocks
        ClientPlayerBlockBreakEvents.AFTER.register { _, _, blockPos, _ ->
            if (!options.renderShapes || options.drawOnBlocks) return@register
            for (shape in shapes) {
                if (shape.isInRange(blockPos.x, blockPos.z)) {
                    shape.shouldRerender = true
                }
            }
        }

        // Listen for block place events to update rendered shape blocks
        UseBlockCallback.EVENT.register { _, _, _, hitResult ->
            if (!options.renderShapes || options.drawOnBlocks) return@register ActionResult.PASS
            val blockPos = hitResult.blockPos
            for (shape in shapes) {
                if (shape.isInRange(blockPos.x, blockPos.z)) {
                    shape.shouldRerender = true
                }
            }

            return@register ActionResult.PASS
        }

        // Load shapes from file when the world is loaded
        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { _, _ ->
            try {
                shapes = SavedDataManager.loadShapes()
                logger.info("Loaded ${shapes.size} shapes from file")
            } catch (e: Exception) {
                logger.error("Failed to load shapes from file", e)
                logger.info("Locking the file to prevent overwriting it")
                overwriteProtection = true
            }

            for (shape in shapes) {
                shape.shouldRerender = true
            }
        })

        ServerWorldEvents.UNLOAD.register(ServerWorldEvents.Unload { _, _ ->
            quadVboMap.clear()
            outlineVboMap.clear()
            shapes.clear()
        })

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            while (menuKeyBinding.wasPressed()) {
                client.setScreen(ShapesScreen(client.currentScreen, shapes))
            }

            while (toggleRenderKeyBinding.wasPressed()) {
                options.renderShapes = !options.renderShapes
                client.player?.sendMessage(
                    Text.literal(
                        if (options.renderShapes) "Shapes are now visible"
                        else "Shapes are now hidden"
                    ), true
                )
            }

            while (rerenderShapesKeyBinding.wasPressed()) {
                rerenderAllShapes()
                client.player?.sendMessage(
                    Text.literal("Rerendered all shapes"), true
                )
            }
        })
    }

    private fun renderShape(context: WorldRenderContext, shape: Shape) {
        val camera = context.camera()
        val transformedPosition = shape.pos.subtract(camera.pos)

        val matrixStack = MatrixStack()
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.pitch))
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.yaw + 180.0f))
        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z)

        if (shape.shouldRerender) {
            val positionMatrix = context.matrixStack()!!.peek().positionMatrix
            val tessellator = Tessellator.getInstance()

            val blocks = shape.generateBlocks()
            shape.blockAmount = blocks.size

            val red = shape.color.red.toFloat() / 255
            val green = shape.color.green.toFloat() / 255
            val blue = shape.color.blue.toFloat() / 255

            // Build outlines for each block
            if (options.drawMode != Options.DrawMode.FACES) {
                val buffer = tessellator.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR)

                for (block in blocks) {
                    if (!options.drawOnBlocks) {
                        val blockPos = BlockPos(block.x.toInt(), block.y.toInt(), block.z.toInt())
                        if (client.world?.getBlockState(blockPos)?.isAir == false) continue
                    }
                    val start = block.subtract(shape.pos)
                    val end = start.add(1.0, 1.0, 1.0)

                    val x1 = start.x.toFloat()
                    val y1 = start.y.toFloat()
                    val z1 = start.z.toFloat()
                    val x2 = end.x.toFloat()
                    val y2 = end.y.toFloat()
                    val z2 = end.z.toFloat()

                    RenderUtil.buildOutline(
                        buffer,
                        positionMatrix,
                        red,
                        green,
                        blue,
                        options.outlineOpacity,
                        x1,
                        y1,
                        z1,
                        x2,
                        y2,
                        z2
                    )
                }

                val outlinesVbo = VertexBuffer(VertexBuffer.Usage.STATIC)
                outlinesVbo.bind()
                outlinesVbo.upload(buffer.end())
                VertexBuffer.unbind()
                outlineVboMap[shape.name] = outlinesVbo
            }

            if (options.drawMode != Options.DrawMode.EDGES) {
                val buffer = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)

                for (block in blocks) {
                    if (!options.drawOnBlocks) {
                        val blockPos = BlockPos(block.x.toInt(), block.y.toInt(), block.z.toInt())
                        if (client.world?.getBlockState(blockPos)?.isAir == false) continue
                    }

                    val start = block.subtract(shape.pos)
                    val end = start.add(1.0, 1.0, 1.0)

                    val x1 = start.x.toFloat()
                    val y1 = start.y.toFloat()
                    val z1 = start.z.toFloat()
                    val x2 = end.x.toFloat()
                    val y2 = end.y.toFloat()
                    val z2 = end.z.toFloat()

                    RenderUtil.buildQuad(
                        buffer,
                        positionMatrix,
                        red,
                        green,
                        blue,
                        options.fillOpacity,
                        x1,
                        y1,
                        z1,
                        x2,
                        y2,
                        z2
                    )
                }

                val quadsVbo = VertexBuffer(VertexBuffer.Usage.STATIC)
                quadsVbo.bind()
                quadsVbo.upload(buffer.end())
                VertexBuffer.unbind()
                quadVboMap[shape.name] = quadsVbo
            }

            shape.shouldRerender = false
        }

        val positionMatrix = matrixStack.peek().positionMatrix

        if (options.drawMode != Options.DrawMode.EDGES && quadVboMap[shape.name] != null) {
            quadVboMap[shape.name]!!.bind()
            quadVboMap[shape.name]!!.draw(
                positionMatrix,
                RenderSystem.getProjectionMatrix(),
                GameRenderer.getPositionColorProgram()
            )
            VertexBuffer.unbind()
        }

        if (options.drawMode != Options.DrawMode.FACES && outlineVboMap[shape.name] != null) {
            val outlineVbo = outlineVboMap[shape.name]!!
            outlineVbo.bind()
            outlineVbo.draw(
                positionMatrix,
                RenderSystem.getProjectionMatrix(),
                GameRenderer.getPositionColorProgram()
            )
            VertexBuffer.unbind()
        }
    }

    fun rerenderAllShapes() {
        if (client.world == null) return
        for (shape in shapes) {
            shape.shouldRerender = true
        }
    }
}