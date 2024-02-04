package org.pkozak.shape

import net.minecraft.util.math.Vec3d
import org.pkozak.Shape
import org.pkozak.ShapeType
import java.awt.Color

class Cube(override var color: Color, override var pos: Vec3d, filled: Boolean, dimensions: Vec3d
) : Shape() {
    override val name = "Cube ${pos.x}, ${pos.y}, ${pos.z}"
    override val type = ShapeType.CUBE

    private var dimensions: Vec3d = dimensions
        set(value) {
            field = value
            render()
        }

    override fun render(): MutableSet<Vec3d> {
        val positions = mutableSetOf<Vec3d>()

        // Render the full cube
        if (filled) {
            for (x in 0 until dimensions.x.toInt()) {
                for (y in 0 until dimensions.y.toInt()) {
                    for (z in 0 until dimensions.z.toInt()) {
                        positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                    }
                }
            }
        } else {
            // Render the outline of the cube
            for (x in 0 until dimensions.x.toInt()) {
                for (y in 0 until dimensions.y.toInt()) {
                    for (z in 0 until dimensions.z.toInt()) {
                        if (x == 0 || x == dimensions.x.toInt() - 1 || y == 0 || y == dimensions.y.toInt() - 1 || z == 0 || z == dimensions.z.toInt() - 1) {
                            positions.add(Vec3d(pos.x + x, pos.y + y, pos.z + z))
                        }
                    }
                }
            }
        }

        return positions
    }
}