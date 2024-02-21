package org.pkozak

import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.pkozak.screen.ShapesScreen
import org.pkozak.util.SavedDataManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object PhantomShapesClient : ClientModInitializer {
    val logger: Logger = LoggerFactory.getLogger("phantomshapes")
    val client: MinecraftClient = MinecraftClient.getInstance()
    val options = Options()

    val CUBE_ICON = Identifier("phantomshapes", "cube")
    val SPHERE_ICON = Identifier("phantomshapes", "sphere")
    val CYLINDER_ICON = Identifier("phantomshapes", "cylinder")
    val TUNNEL_ICON = Identifier("phantomshapes", "tunnel")
    val VISIBLE_ICON = Identifier("phantomshapes", "eye_open")
    val INVISIBLE_ICON = Identifier("phantomshapes", "eye_closed")
    val ARCH_BRIDGE_ICON = Identifier("phantomshapes", "arch")
    val PIN_ICON = Identifier("phantomshapes", "pin")
    val DELETE_ICON = Identifier("phantomshapes", "delete")

    var overwriteProtection = false

    private var shapes = mutableListOf<Shape>()
    private var vboMap = mutableMapOf<String, VertexBuffer>()

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

    override fun onInitializeClient() {
        WorldRenderEvents.LAST.register {
            val matrixStack = it.matrixStack()
            val projectionMatrix = it.projectionMatrix()
            onWorldRendered(matrixStack, projectionMatrix)
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
            vboMap.clear()
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
        })
    }

    private fun onWorldRendered(matrixStack: MatrixStack, projectionMatrix: Matrix4f) {
        if (!options.renderShapes.value) return

        matrixStack.push()
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        RenderSystem.enableDepthTest()
        RenderSystem.depthFunc(GL11.GL_LEQUAL)
//        RenderSystem.enableDepthTest()
//        RenderSystem.enableCull()
        RenderSystem.disableCull()
        RenderSystem.depthMask(false)
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)

        renderShapeBlocks(matrixStack, projectionMatrix)

        RenderSystem.disableBlend()
//        RenderSystem.depthMask(true)
//        RenderSystem.disableDepthTest()
//        RenderSystem.defaultBlendFunc()
        RenderSystem.enableCull()
        RenderSystem.depthMask(true);
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

            // Shape data changed, rerender the VBO
            if (shape.shouldRerender) {
                val buffer = Tessellator.getInstance().buffer
                buffer.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)

                val blocks = shape.generateBlocks()

                // Add vertices directly to the buffer
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

                    val redFill = shape.color.red.toFloat() / 255
                    val greenFill = shape.color.green.toFloat() / 255
                    val blueFill = shape.color.blue.toFloat() / 255
                    val alphaFill = 0.4f

                    buffer.vertex(rotatedMatrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

                    buffer.vertex(rotatedMatrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()

                    buffer.vertex(rotatedMatrix, x2, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

                    buffer.vertex(rotatedMatrix, x2, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()

                    buffer.vertex(rotatedMatrix, x1, y2, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y2, z2).color(redFill, greenFill, blueFill, alphaFill).next()

                    buffer.vertex(rotatedMatrix, x1, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z1).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x2, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                    buffer.vertex(rotatedMatrix, x1, y1, z2).color(redFill, greenFill, blueFill, alphaFill).next()
                }

                val builtBuffer = buffer.end()

                if (vboMap[shape.name] == null) {
                    val vbo = VertexBuffer(VertexBuffer.Usage.STATIC)
                    vbo.bind()
                    vbo.upload(builtBuffer)
                    VertexBuffer.unbind()
                    vboMap[shape.name] = vbo
                } else {
                    val vbo = vboMap[shape.name]!!
                    vbo.bind()
                    vbo.upload(builtBuffer)
                    VertexBuffer.unbind()
                }

                matrixStack.pop()
                shape.shouldRerender = false
            }

            // Render the VBO
            val vbo = vboMap[shape.name] ?: throw RuntimeException("VBO is null, this should not happen!")
            vbo.bind()
            vbo.draw(
                posMatrix,
                projectionMatrix,
                RenderSystem.getShader()
            )
            VertexBuffer.unbind()
        }
    }
}