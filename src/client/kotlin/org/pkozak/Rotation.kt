package org.pkozak

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import net.minecraft.util.math.Vec3d
import org.joml.Matrix4f
import org.joml.Vector3f

class Rotation() {
    private val rotationMatrix = Matrix4f()
    var x = 0
        set(value) {
            field = value
            updateMatrix()
        }

    var y = 0
        set(value) {
            field = value
            updateMatrix()
        }

    var z = 0
        set(value) {
            field = value
            updateMatrix()
        }

    constructor(x: Int, y: Int, z: Int) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    /**
     * Rotates a point using the stored rotation matrix, around the given origin
     * @param vec The point to rotate
     * @param origin The origin to rotate around
     */
    fun rotatePoint(vec: Vec3d, origin: Vec3d): Vec3d {
        val result = Vector3f()
        val origin3f = Vector3f(origin.x.toFloat(), origin.y.toFloat(), origin.z.toFloat())
        val point = Vector3f(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat()).sub(
            origin3f
        )
        rotationMatrix.transformPosition(point, result) // Transform point using the rotation matrix
        result.add(origin3f) // Add the origin back to the result so the shape is not at 0,0,0
        return Vec3d(result.x.toDouble(), result.y.toDouble(), result.z.toDouble())
    }

    /**
     * Updates the rotation matrix based on the current x, y, and z rotation angles.
     */
    private fun updateMatrix() {
        rotationMatrix.identity() // Reset the matrix to the identity matrix
        if (x != 0) {
            val xRadians = Math.toRadians(x.toDouble()).toFloat()
            rotationMatrix.rotate(xRadians, 1f, 0f, 0f)
        }
        if (y != 0) {
            val yRadians = Math.toRadians(y.toDouble()).toFloat()
            rotationMatrix.rotate(yRadians, 0f, 1f, 0f)
        }
        if (z != 0) {
            val zRadians = Math.toRadians(z.toDouble()).toFloat()
            rotationMatrix.rotate(zRadians, 0f, 0f, 1f)
        }
    }

    fun isZero(): Boolean {
        return x == 0 && y == 0 && z == 0
    }

    fun toJsonObject(): JsonElement {
        val jsonObject = buildJsonArray {
            add(x)
            add(y)
            add(z)
        }
        return jsonObject
    }

    override fun toString(): String {
        return "Rotation{$x, $y, $z}"
    }

    companion object {
        fun fromString(string: String): Rotation {
            // First check for legacy rotation format
            val rotationY = string.toIntOrNull()
            if (rotationY != null) {
                return Rotation(0, rotationY, 0)
            }

            try {
                val parts = string.substring(1, string.length - 1).split(",")
                return Rotation(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } catch (e: Exception) {
                return Rotation(0, 0, 0)
            }
        }
    }
}