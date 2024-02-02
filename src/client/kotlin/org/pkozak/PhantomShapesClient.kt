package org.pkozak

import me.x150.renderer.event.RenderEvents
import me.x150.renderer.render.Renderer3d
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import org.pkozak.shape.Cube
import org.pkozak.shape.Cylinder
import org.pkozak.shape.Sphere
import org.slf4j.LoggerFactory
import java.awt.Color

object PhantomShapesClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("phantomshapes")
    private var shapes = mutableListOf<Shape>()

    override fun onInitializeClient() {
        // Register our first shape
        shapes.add(Cube(Color.RED, Vec3d(0.0, 0.0, 0.0), false, Vec3d(5.0, 5.0, 5.0)))
        // Register our second shape
        shapes.add(Sphere(Color.BLUE, Vec3d(0.0, -20.0, 0.0), false, 5))
        shapes.add(Cylinder(Color.GREEN, Vec3d(20.0, -20.0, 0.0), 5, 10))

        RenderEvents.WORLD.register { matrixStack -> onWorldRendered(matrixStack) }
    }

    // Render the shape
    private fun onWorldRendered(matrix: MatrixStack) {
        for (shape in shapes) {
            val fillColor = Color(shape.color.red, shape.color.green, shape.color.blue, 64)
            for (position in shape.render()) {
                if (!shape.drawOnlyEdge) {
                    Renderer3d.renderFilled(matrix, fillColor, position, Vec3d(1.0, 1.0, 1.0))
                }
                Renderer3d.renderOutline(matrix, shape.color, position, Vec3d(1.0, 1.0, 1.0))
            }
        }
    }
}