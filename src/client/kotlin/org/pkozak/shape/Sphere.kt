package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.minecraft.util.math.Vec3d
import org.pkozak.util.MathUtil.Companion.lengthSq
import java.awt.Color
import kotlin.math.ceil

class Sphere(
    override var name: String, override var color: Color, override var pos: Vec3d, override var radius: Int
) : RadialShape() {
    override val type = ShapeType.SPHERE

    override fun generateBlocks(): MutableSet<Vec3d> {
        val invRadius: Double = 1 / (radius.toDouble() + 0.5)
        val ceilRadius = ceil(radius + 0.5).toInt()

        val positions = mutableSetOf<Vec3d>()
        var nextXn = 0.0
        forX@ for (x in 0..ceilRadius) {
            val xn = nextXn
            nextXn = (x + 1) * invRadius
            var nextYn = 0.0
            forY@ for (y in 0..ceilRadius) {
                val yn = nextYn
                nextYn = (y + 1) * invRadius
                var nextZn = 0.0
                forZ@ for (z in 0..ceilRadius) {
                    val zn = nextZn
                    nextZn = (z + 1) * invRadius

                    val distanceSq: Double = lengthSq(xn, yn, zn)
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break@forX
                            }
                            break@forY
                        }
                        break@forZ
                    }


                    if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(
                            xn, yn, nextZn
                        ) <= 1
                    ) {
                        continue
                    }

                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x + x, pos.y - y, pos.z + z))
                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z - z))
                    positions.add(Vec3d(pos.x - x, pos.y - y, pos.z + z))
                    positions.add(Vec3d(pos.x + x, pos.y - y, pos.z - z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z - z))
                    positions.add(Vec3d(pos.x - x, pos.y - y, pos.z - z))
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
            put("enabled", enabled)
        }

        return json
    }
}
