package org.pkozak

class Util {
    companion object {
        fun lengthSq(x: Double, y: Double, z: Double): Double {
            return (x * x) + (y * y) + (z * z)
        }

        fun lengthSq(x: Double, z: Double): Double {
            return (x * x) + (z * z)
        }
    }
}