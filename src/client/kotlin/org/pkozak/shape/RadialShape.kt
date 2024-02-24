package org.pkozak.shape

abstract class RadialShape : Shape() {
    abstract var radius: Int

    override fun isInRange(x: Int, z: Int): Boolean {
        val r = radius + 1
        val dx = x - pos.x
        val dz = z - pos.z
        return dx * dx + dz * dz <= r * r
    }
}