package org.pkozak

import net.minecraft.util.math.Vec3d
import java.awt.Color

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    abstract val name: String
    abstract val type: ShapeType
    var filled = false
    var enabled = true
    var drawOnlyEdge = false

    abstract fun render(): MutableSet<Vec3d>
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, TUNNEL, CONE, PYRAMID;
}
