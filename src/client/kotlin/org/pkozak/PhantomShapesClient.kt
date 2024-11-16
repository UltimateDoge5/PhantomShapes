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
import net.minecraft.client.gl.GlUsage
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.pkozak.screen.ShapesScreen
import org.pkozak.shape.Shape
import org.pkozak.util.RenderUtil
import org.pkozak.util.SavedDataManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.abs


object PhantomShapesClient : ClientModInitializer {
    val logger: Logger = LoggerFactory.getLogger("phantomshapes")
    private val client: MinecraftClient = MinecraftClient.getInstance()
    val options = Options()

    var overwriteProtection = false

    private var shapes = mutableListOf<Shape>()
    private var shapeBlockCache = mutableMapOf<String, Set<Vec3d>>()
    private var quadVboMap = mutableMapOf<String, VertexBuffer>()
    private var outlineVboMap = mutableMapOf<String, VertexBuffer>()

    private const val POSITION_THRESHOLD: Double = 0.04 // Minimum distance change to trigger re-sort
    private const val ROTATION_THRESHOLD: Double = 0.5 // Minimum rotation angle change to trigger re-sort

    private var lastCameraPosition: Vec3d? = null
    private var lastCameraYaw = 0f
    private var lastCameraPitch = 0f

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

            val camera = context.camera()

            if (options.drawMode != Options.DrawMode.EDGES && needsResort(camera.pos, camera.yaw, camera.pitch)) {
                // Check which shapes are on the screen
                for (shape in shapes) {
                    val distance = shape.pos.distanceTo(camera.pos)
                    val renderDistance = MinecraftClient.getInstance().options.viewDistance.value * 16

                    // Skip rendering if the shape is disabled or too far away
                    if (!shape.enabled || distance > renderDistance) continue
                    //TODO: Explore potential optimization by checking if the shape is in the camera's view frustum
                    shape.shouldReorder = true
                }
            }

            context.matrixStack()!!.push()
            RenderSystem.enableBlend()
            RenderSystem.enableCull()
            RenderSystem.depthMask(true)
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(GL11.GL_LEQUAL)
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)

            for (shape in shapes) {
                val distance = shape.pos.distanceTo(context.camera().pos)
                val renderDistance = MinecraftClient.getInstance().options.viewDistance.value * 16

                // Skip rendering if the shape is disabled or too far away
                if (!shape.enabled || distance > renderDistance) continue
                renderShape(context, shape)
            }

            updateLastCameraState(camera.pos, camera.yaw, camera.pitch)

            RenderSystem.disableBlend()
            RenderSystem.disableDepthTest()
            context.matrixStack()!!.pop()
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

        if (shape.shouldRerender || shape.shouldReorder) {
            val positionMatrix = context.matrixStack()!!.peek().positionMatrix
            val tessellator = Tessellator.getInstance()

            // If the shape is not in the cache or a rerender is requested, generate the blocks
            val blocks: List<Vec3d> = if (shapeBlockCache.containsKey(shape.name) && shape.shouldReorder) {
                shape.shouldReorder = false
                shapeBlockCache[shape.name]!!
            } else {
                val blockList = shape.generateBlocks()
                shapeBlockCache[shape.name] = blockList
                blockList
            }.sortedBy { it.distanceTo(camera.pos) } // Sort the blocks by distance to the camera

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
                        val blockState = client.world?.getBlockState(blockPos) ?: continue
                        if (!blockState.isAir && !blockState.isReplaceable) continue
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

                // Reuse the VBO if it already exists
                val outlinesVbo = outlineVboMap[shape.name] ?: VertexBuffer(GlUsage.DYNAMIC_WRITE)
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
                        val blockState = client.world?.getBlockState(blockPos) ?: continue
                        if (!blockState.isAir && !blockState.isReplaceable) continue
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

                // Reuse the VBO if it already exists
                val quadsVbo = quadVboMap[shape.name] ?: VertexBuffer(GlUsage.DYNAMIC_WRITE)
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
                RenderSystem.getShader()
            )
            VertexBuffer.unbind()
        }

        if (options.drawMode != Options.DrawMode.FACES && outlineVboMap[shape.name] != null) {
            val outlineVbo = outlineVboMap[shape.name]!!
            outlineVbo.bind()
            outlineVbo.draw(
                positionMatrix,
                RenderSystem.getProjectionMatrix(),
                RenderSystem.getShader()
            )
            VertexBuffer.unbind()
        }
    }

    private fun needsResort(currentPos: Vec3d, currentYaw: Float, currentPitch: Float): Boolean {
        if (lastCameraPosition == null) {
            updateLastCameraState(currentPos, currentYaw, currentPitch)
            return false
        }
        val distanceMoved = currentPos.distanceTo(lastCameraPosition)
        val yawChange = abs((currentYaw - lastCameraYaw).toDouble()).toFloat()
        val pitchChange = abs((currentPitch - lastCameraPitch).toDouble()).toFloat()

        return distanceMoved > POSITION_THRESHOLD || yawChange > ROTATION_THRESHOLD || pitchChange > ROTATION_THRESHOLD
    }

    private fun updateLastCameraState(position: Vec3d, yaw: Float, pitch: Float) {
        this.lastCameraPosition = position
        this.lastCameraYaw = yaw
        this.lastCameraPitch = pitch
    }

    fun rerenderAllShapes() {
        if (client.world == null) return
        for (shape in shapes) {
            shape.shouldRerender = true
        }
    }
}