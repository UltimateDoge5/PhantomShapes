package org.pkozak.shape

import kotlinx.serialization.json.*
import net.minecraft.util.math.Vec3d
import org.pkozak.Shape
import org.pkozak.ShapeType
import java.awt.Color

class Cube(
    override val name: String, override var color: Color, override var pos: Vec3d, dimensions: Vec3d
) : Shape() {
    override val type = ShapeType.CUBE

    private var dimensions: Vec3d = dimensions
        set(value) {
            field = value
            render()
        }

    override fun render(): MutableSet<Vec3d> {
        val positions = mutableSetOf<Vec3d>()

        // Render the full cube
        if (filled) {
            for (x in 0 until dimensions.x.toInt()) {
                for (y in 0 until dimensions.y.toInt()) {
                    for (z in 0 until dimensions.z.toInt()) {
                        positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    }
                }
            }
        } else {
            // Render the outline of the cube
            for (x in 0 until dimensions.x.toInt()) {
                for (y in 0 until dimensions.y.toInt()) {
                    for (z in 0 until dimensions.z.toInt()) {
                        if (x == 0 || x == dimensions.x.toInt() - 1 || y == 0 || y == dimensions.y.toInt() - 1 || z == 0 || z == dimensions.z.toInt() - 1) {
                            positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                        }
                    }
                }
            }
        }

        return positions
    }

    override fun toJsonObject(): JsonObject {
        val json = buildJsonObject {
            put("name", name)
            put("type", type.toString())
            put("color", color.rgb)
            put("pos", buildJsonObject {
                put("x", pos.x)
                put("y", pos.y)
                put("z", pos.z)
            })
            put("dimensions", buildJsonObject {
                put("x", dimensions.x)
                put("y", dimensions.y)
                put("z", dimensions.z)
            })
            put("filled", filled)
        }

        return json
    }
}
