package org.pkozak

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
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
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
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
            "key.phantomshapes.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, "category.phantomshapes.controls"
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
        WorldRenderEvents.LAST.register {
            onWorldRendered(it.matrixStack(), it.projectionMatrix())
        }

        // Listen for block break events to update rendered shape blocks
        ClientPlayerBlockBreakEvents.AFTER.register { _, _, blockPos, _ ->
            if (!options.renderShapes.value || options.drawOnBlocks.value) return@register
            for (shape in shapes) {
                if (shape.isInRange(blockPos.x, blockPos.z)) {
                    shape.shouldRerender = true
                }
            }
        }

        // Listen for block place events to update rendered shape blocks
        UseBlockCallback.EVENT.register { _, _, _, hitResult ->
            if (!options.renderShapes.value || options.drawOnBlocks.value) return@register ActionResult.PASS
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
            shapes.clear()
        })

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            while (menuKeyBinding.wasPressed()) {
                client.setScreen(ShapesScreen(client.currentScreen, shapes))
            }

            while (toggleRenderKeyBinding.wasPressed()) {
                options.renderShapes.value = !options.renderShapes.value
                client.player?.sendMessage(
                    Text.literal(
                        if (options.renderShapes.value) "Shapes are now visible"
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

    private fun onWorldRendered(matrixStack: MatrixStack, projectionMatrix: Matrix4f) {
        if (!options.renderShapes.value) return

        matrixStack.push()
        RenderSystem.enableBlend()
        RenderSystem.enableDepthTest()
        RenderSystem.enableCull()
        RenderSystem.depthMask(false)
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)

        renderShapeBlocks(matrixStack, projectionMatrix)

        RenderSystem.disableBlend()
        RenderSystem.depthMask(true)
        matrixStack.pop()
    }

    private fun renderShapeBlocks(matrixStack: MatrixStack, projectionMatrix: Matrix4f) {
        val playerPos = MinecraftClient.getInstance().player?.pos
        val camera = client.gameRenderer.camera
        val posMatrix = matrixStack.peek().positionMatrix
            .translate(
                -(camera.pos.x).toFloat(),
                -(camera.pos.y).toFloat(),
                -(camera.pos.z).toFloat()
            )

        for (shape in shapes) {
            val distance = shape.pos.distanceTo(playerPos)
            val renderDistance = MinecraftClient.getInstance().options.viewDistance.value * 16

            // Skip rendering if the shape is disabled or too far away
            if (!shape.enabled || distance > renderDistance) continue

            matrixStack.push()

            val rotatedMatrix = Matrix4f()
                .translate(
                    -(camera.pos.x).toFloat(),
                    -(camera.pos.y).toFloat(),
                    -(camera.pos.z).toFloat()
                )

            // Shape data changed, rerender the VBOs
            if (shape.shouldRerender) {
                val buffer = Tessellator.getInstance().buffer

                val blocks = shape.generateBlocks()
                shape.blockAmount = blocks.size

                val red = shape.color.red.toFloat() / 255
                val green = shape.color.green.toFloat() / 255
                val blue = shape.color.blue.toFloat() / 255
                val alpha = 0.4f

                // Build outlines for each block
                if (options.drawMode.value != Options.DrawMode.FACES) {
                    buffer.begin(DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR)
                    for (block in blocks) {
                        if (!options.drawOnBlocks.value) {
                            val blockPos = BlockPos(block.x.toInt(), block.y.toInt(), block.z.toInt())
                            if (client.world?.getBlockState(blockPos)?.isAir == false) continue
                        }

                        val start = block.add(client.gameRenderer.camera.pos)
                        val end = start.add(1.0, 1.0, 1.0)

                        val x1 = start.x.toFloat()
                        val y1 = start.y.toFloat()
                        val z1 = start.z.toFloat()
                        val x2 = end.x.toFloat()
                        val y2 = end.y.toFloat()
                        val z2 = end.z.toFloat()

                        RenderUtil.buildOutline(buffer, rotatedMatrix, red, green, blue, 1f, x1, y1, z1, x2, y2, z2)
                    }

                    val builtBuffer = buffer.end()

                    // Create outline VBO if it doesn't exist, otherwise update it
                    if (outlineVboMap[shape.name] == null) {
                        outlineVboMap[shape.name] = RenderUtil.createVBO(builtBuffer)
                    } else {
                        outlineVboMap[shape.name]!!.bind()
                        outlineVboMap[shape.name]!!.upload(builtBuffer)
                        VertexBuffer.unbind()
                    }
                }

                // Now we can build quads for each block
                if (options.drawMode.value != Options.DrawMode.EDGES) {
                    buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
                    for (block in blocks) {
                        if (!options.drawOnBlocks.value) {
                            val blockPos = BlockPos(block.x.toInt(), block.y.toInt(), block.z.toInt())
                            if (client.world?.getBlockState(blockPos)?.isAir == false) continue
                        }

                        val start = block.add(client.gameRenderer.camera.pos)
                        val end = start.add(1.0, 1.0, 1.0)

                        val x1 = start.x.toFloat()
                        val y1 = start.y.toFloat()
                        val z1 = start.z.toFloat()
                        val x2 = end.x.toFloat()
                        val y2 = end.y.toFloat()
                        val z2 = end.z.toFloat()

                        RenderUtil.buildQuad(buffer, rotatedMatrix, red, green, blue, alpha, x1, y1, z1, x2, y2, z2)
                    }

                    val builtBuffer = buffer.end()

                    if (quadVboMap[shape.name] == null) {
                        quadVboMap[shape.name] = RenderUtil.createVBO(builtBuffer)
                    } else {
                        quadVboMap[shape.name]!!.bind()
                        quadVboMap[shape.name]!!.upload(builtBuffer)
                        VertexBuffer.unbind()
                    }
                }

                matrixStack.pop()
                shape.shouldRerender = false
            }

            // Render quads using the VBO
            if (options.drawMode.value != Options.DrawMode.EDGES && quadVboMap[shape.name] != null) {
                val vbo = quadVboMap[shape.name]!!
                vbo.bind()
                vbo.draw(
                    posMatrix,
                    projectionMatrix,
                    RenderSystem.getShader()
                )
                VertexBuffer.unbind()
            }

            // Render outlines using the outline VBO
            if(options.drawMode.value != Options.DrawMode.FACES && outlineVboMap[shape.name] != null) {
                val outlineVbo = outlineVboMap[shape.name]!!
                outlineVbo.bind()
                outlineVbo.draw(
                    posMatrix,
                    projectionMatrix,
                    RenderSystem.getShader()
                )
                VertexBuffer.unbind()
            }
        }
    }

    fun rerenderAllShapes() {
        for (shape in shapes) {
            shape.shouldRerender = true
        }
    }
}