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
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.Vec3d
import org.lwjgl.glfw.GLFW
import org.pkozak.screen.ShapesScreen
import org.pkozak.shape.Cube
import org.pkozak.shape.Cylinder
import org.pkozak.shape.Sphere
import org.slf4j.LoggerFactory
import java.awt.Color


object PhantomShapesClient : ClientModInitializer {
    val logger = LoggerFactory.getLogger("phantomshapes")
    private var shapes = mutableListOf<Shape>()

    private var keyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.phantomshapes.menu",  // The translation key of the keybinding's name
            InputUtil.Type.KEYSYM,  // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
            GLFW.GLFW_KEY_P,  // The keycode of the key
            "category.phantomshapes.controls" // The translation key of the keybinding's category.
        )
    )

    override fun onInitializeClient() {
        RenderEvents.WORLD.register { matrixStack -> onWorldRendered(matrixStack) }

        ServerWorldEvents.LOAD.register(ServerWorldEvents.Load { _, _ ->
            try {
                shapes = SavedDataManager.loadShapes().toMutableList()
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
            if (!shape.enabled) continue
            val cubes = shape.render()
            val fillColor = Color(shape.color.red, shape.color.green, shape.color.blue, 50)
            for (cube in cubes) {
                Renderer3d.renderEdged(matrix, fillColor, shape.color, cube, Vec3d(1.0, 1.0, 1.0))
            }
        }
    }
}