package org.pkozak.shape

import kotlinx.serialization.json.JsonObject
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.Rotation
import org.pkozak.ui.Icons
import java.awt.Color

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    abstract var name: String
    abstract val type: ShapeType
    var enabled = true
    var rotation = Rotation(0, 0, 0) // Every shape has a rotation, even if it's not used
    var shouldRerender = true
    var shouldReorder = false
    var blockAmount = -1 // -1 means unknown - the amount wasn't calculated yet

    abstract fun generateBlocks(): MutableSet<Vec3d>

    fun generateTransformedBlocks(): MutableSet<Vec3d> {
        val blocks = generateBlocks()
        val transformedBlocks = mutableSetOf<Vec3d>()

        if (rotation.isZero()) {
            return blocks
        }

        for (block in blocks) {
            transformedBlocks.add(rotation.rotatePoint(block, pos))
        }

        return transformedBlocks
    }

    abstract fun isInRange(x: Int, z: Int): Boolean

    abstract fun getBoundingBox(): Box

    fun toggleVisibility() {
        enabled = !enabled
        shouldRerender = true
    }

    fun getIcon() = when (type) {
        ShapeType.CUBE -> Icons.CUBE_ICON
        ShapeType.SPHERE -> Icons.SPHERE_ICON
        ShapeType.CYLINDER -> Icons.CYLINDER_ICON
        ShapeType.TUNNEL -> Icons.TUNNEL_ICON
        ShapeType.ARCH -> Icons.ARCH_ICON
        ShapeType.HEXAGON -> Icons.HEXAGON_ICON
    }

    abstract fun toJsonObject(): JsonObject

    companion object {
        // TODO: Make this safer - it's not safe to assume that the JsonObject will always contain the required fields
        fun fromJsonObject(json: JsonObject): Shape {
            // For some reason, the type is wrapped in quotes
            val type = ShapeType.valueOf(json["type"].toString().replace("\"", ""))
            val name = json["name"].toString().replace("\"", "") // Same here

            val color = Color(json["color"].toString().toInt())
            val pos = Vec3d(
                (json["pos"] as JsonObject)["x"].toString().toDouble(),
                (json["pos"] as JsonObject)["y"].toString().toDouble(),
                (json["pos"] as JsonObject)["z"].toString().toDouble()
            )
            val enabled = json["enabled"].toString().toBoolean()

            return when (type) {
                ShapeType.CUBE -> {
                    val dimensions = Vec3i(
                        (json["dimensions"] as JsonObject)["x"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["y"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["z"].toString().toInt()
                    )

                    Cube(name, color, pos, dimensions).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.SPHERE -> {
                    val radius = json["radius"].toString().toInt()

                    Sphere(name, color, pos, radius).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.CYLINDER -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()

                    Cylinder(name, color, pos, radius, height).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.TUNNEL -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val rotation = Rotation.fromString(json["rotation"].toString())

                    Tunnel(name, color, pos, radius, height).apply {
                        this.rotation = rotation
                        this.enabled = enabled
                    }
                }

                ShapeType.ARCH -> {
                    val radius = json["radius"].toString().toInt()
                    val width = json["width"].toString().toInt()
                    val rotation = Rotation.fromString(json["rotation"].toString())

                    Arch(name, color, pos, radius, width).apply {
                        this.rotation = rotation
                        this.enabled = enabled
                    }
                }

                ShapeType.HEXAGON -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()

                    Hexagon(name, color, pos, radius, height).apply {
                        this.enabled = enabled
                    }
                }
            }
        }

        fun getIcon(type: ShapeType) = when (type) {
            ShapeType.CUBE -> Icons.CUBE_ICON
            ShapeType.SPHERE -> Icons.SPHERE_ICON
            ShapeType.CYLINDER -> Icons.CYLINDER_ICON
            ShapeType.TUNNEL -> Icons.TUNNEL_ICON
            ShapeType.ARCH -> Icons.ARCH_ICON
            ShapeType.HEXAGON -> Icons.HEXAGON_ICON
        }
    }
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, TUNNEL, ARCH, HEXAGON
}
