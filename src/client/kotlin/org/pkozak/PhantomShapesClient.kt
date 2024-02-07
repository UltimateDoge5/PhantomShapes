package org.pkozak

import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.Renderer3d
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.WorldSavePath
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.lwjgl.glfw.GLFW
import org.pkozak.screen.ShapesScreen
import org.pkozak.shape.Tunnel
import org.slf4j.LoggerFactory
import java.awt.Color


object PhantomShapesClient : ClientModInitializer {
    val logger = LoggerFactory.getLogger("phantomshapes")

    val CUBE_ICON = Identifier("phantomshapes", "cube")
    val SPHERE_ICON = Identifier("phantomshapes", "sphere")
    val CYLINDER_ICON = Identifier("phantomshapes", "cylinder")
    val TUNNEL_ICON = Identifier("phantomshapes", "tunnel")
    val VISIBLE_ICON = Identifier("phantomshapes", "eye_open")
    val INVISIBLE_ICON = Identifier("phantomshapes", "eye_closed")
    val DELETE_ICON = Identifier("phantomshapes", "delete")

    private var shapes = mutableListOf<Shape>()

    private var keyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.phantomshapes.menu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.phantomshapes.controls"
        )
    )

    override fun onInitializeClient() {
        RenderEvents.WORLD.register { matrixStack -> onWorldRendered(matrixStack) }

        // Load shapes from file when the world is loaded
        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { _, _ ->
            try {
                shapes = SavedDataManager.loadShapes()
            } catch (e: Exception) {
                logger.error("Failed to load shapes from file", e)
            }
        })

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            while (keyBinding.wasPressed()) {
                client.setScreen(ShapesScreen(client.currentScreen, shapes))
            }
        })
    }

    // Render shapes from the cache
    private fun onWorldRendered(matrix: MatrixStack) {
        for (shape in shapes) {
            val playerPos = MinecraftClient.getInstance().player?.pos
            val distance = shape.pos.distanceTo(playerPos)
            val renderDistance = MinecraftClient.getInstance().options.viewDistance.value * 16

            // Skip rendering if the shape is disabled or too far away
            if (!shape.enabled || distance > renderDistance) continue

            val blocks = shape.render()
            val fillColor = Color(shape.color.red, shape.color.green, shape.color.blue, 50)
            for (cube in blocks) {
                Renderer3d.renderEdged(matrix, fillColor, shape.color, cube, Vec3d(1.0, 1.0, 1.0))
            }
        }
    }
}