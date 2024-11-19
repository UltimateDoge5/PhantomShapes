package org.pkozak.shape

import kotlinx.serialization.json.*
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import java.awt.Color

class Cube(
    override var name: String, override var color: Color, override var pos: Vec3d, var dimensions: Vec3i
) : Shape() {
    override val type = ShapeType.CUBE

    override fun generateBlocks(): MutableSet<Vec3d> {
        val positions = mutableSetOf<Vec3d>()


        // Render the outline of the cube
        for (x in 0 until dimensions.x) {
            for (y in 0 until dimensions.y) {
                for (z in 0 until dimensions.z) {
                    if (x == 0 || x == dimensions.x - 1 || y == 0 || y == dimensions.y - 1 || z == 0 || z == dimensions.z - 1) {
                        positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    }
                }
            }
        }
        return positions
    }

    override fun isInRange(x: Int, z: Int): Boolean {
        return x >= pos.x && x <= pos.x + dimensions.x && z >= pos.z && z <= pos.z + dimensions.z
    }

    override fun getBoundingBox(): Box {
        return Box(pos.x, pos.y, pos.z, pos.x + dimensions.x, pos.y + dimensions.y, pos.z + dimensions.z)
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
            put("enabled", enabled)
        }

        return json
    }
}
