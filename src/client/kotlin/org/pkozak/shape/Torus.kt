package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.round
import kotlin.math.sin

class Torus(
    override var name: String,
    override var color: Color,
    override var pos: Vec3d,
    override var radius: Int,
    var minorRadius: Int
) : RadialShape() {
    override val type: ShapeType = ShapeType.TORUS

    // Based and adapted from https://electronut.in/rendering-a-torus-geometry-lighting-and-textures/
    override fun generateBlocks(): MutableSet<Vec3d> {
        val blocks = mutableSetOf<Vec3d>()

        // Scale the steps with the radius (steps into two variables for more efficient loops)
        val radialSteps = max(200, radius * 20)
        val tubularSteps = max(200, minorRadius * 20)

        for (i in 0 until radialSteps) {
            val u = 2 * Math.PI * i / radialSteps
            for (j in 0 until tubularSteps) {
                val v = 2 * Math.PI * j / tubularSteps
                val x = (radius + minorRadius * cos(v)) * cos(u)
                val y = (radius + minorRadius * cos(v)) * sin(u)
                val z = minorRadius * sin(v)
                blocks.add(Vec3d(round(pos.x + x), round(pos.y + y), round(pos.z + z)))
            }
        }

        return blocks
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
            put("minorRadius", minorRadius)
            put("enabled", enabled)
            put("rotation", rotation.toJsonObject())
        }

        return json
    }
}