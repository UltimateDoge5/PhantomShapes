package org.pkozak.shape

import net.minecraft.util.math.Box

abstract class RadialShape : Shape() {
    abstract var radius: Int

    override fun isInRange(x: Int, z: Int): Boolean {
        val r = radius + 1
        val dx = x - pos.x
        val dz = z - pos.z
        return dx * dx + dz * dz <= r * r
    }

    override fun getBoundingBox(): Box {
        val r = radius + 1
        return Box(pos.x - r, pos.y, pos.z - r, pos.x + r, pos.y + 1, pos.z + r)
    }
}