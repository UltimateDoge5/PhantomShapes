package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.util.math.Vec3d
import org.pkozak.util.MathUtil
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

class Arch(
    override var name: String,
    override var color: Color,
    override var pos: Vec3d,
    override var radius: Int,
    var width: Int
) : RadialShape() {
    override val type = ShapeType.ARCH
    override fun generateBlocks(): MutableSet<Vec3d> {
        val positions = mutableSetOf<Vec3d>()

        val invRadius: Double = 1 / (radius + 0.5)
        val ceilRadius = ceil(radius + 0.5).toInt()

        var nextXn = 0.0
        forX@ for (x in 0..ceilRadius) {
            val xn = nextXn
            nextXn = (x + 1) * invRadius
            var nextZn = 0.0
            forY@ for (y in 0..ceilRadius) {
                val zn = nextZn
                nextZn = (y + 1) * invRadius

                if (MathUtil.lengthSq(nextXn, zn) <= 1 && MathUtil.lengthSq(xn, nextZn) <= 1) {
                    continue
                }

                for (z in 0 until width) {
                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z - z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z - z))
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
            put("radius", radius)
            put("width", width)
            put("rotation", rotation.toJsonObject())
            put("enabled", enabled)
        }

        return json
    }
}