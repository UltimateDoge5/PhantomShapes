package org.pkozak.shape

import net.minecraft.util.math.Vec3d
import org.pkozak.Shape
import org.pkozak.Util.Companion.lengthSq
import java.awt.Color
import kotlin.math.ceil


class Cylinder(
    override var color: Color,
    override var pos: Vec3d,
    var radius: Int,
    var height: Int
) : Shape() {
    private val positions = mutableSetOf<Vec3d>()

    override fun render(): MutableSet<Vec3d> {
        renderCylinder()
        return positions
    }

    private fun renderCylinder(): MutableSet<Vec3d> {
        val invRadius: Double = 1 / (radius + 0.5)
        val ceilRadius = ceil(radius + 0.5).toInt()

        var nextXn = 0.0
        forX@ for (x in 0..ceilRadius) {
            val xn = nextXn
            nextXn = (x + 1) * invRadius
            var nextZn = 0.0
            forZ@ for (z in 0..ceilRadius) {
                val zn = nextZn
                nextZn = (z + 1) * invRadius

                val distanceSq: Double = lengthSq(xn, zn)
                if (distanceSq > 1) {
                    if (z == 0) {
                        break@forX
                    }
                    break@forZ
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue
                    }
                }

                for (y in 0 until height) {
                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z + z))
                    positions.add(Vec3d(pos.x + x, pos.y + y, pos.z - z))
                    positions.add(Vec3d(pos.x - x, pos.y + y, pos.z - z))
                }
            }
        }


        return positions
    }
}
