package org.pkozak

import net.minecraft.util.math.Vec3d
import java.awt.Color

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    var filled = false
    var drawOnlyEdge = false

    abstract fun render(): MutableSet<Vec3d>
}
