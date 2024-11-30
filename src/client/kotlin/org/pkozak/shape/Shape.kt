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
        ShapeType.ARCH -> Icons.ARCH_ICON
        ShapeType.POLYGON -> Icons.HEXAGON_ICON
    }

    abstract fun toJsonObject(): JsonObject

    companion object {
        // TODO: Make this safer - it's not safe to assume that the JsonObject will always contain the required fields
        fun fromJsonObject(json: JsonObject): Shape {
            // For some reason, the type is wrapped in quotes
            val typeString = json["type"].toString().replace("\"", "")

            // If the type is unknown, try to migrate an older shape
            val type = try {
                ShapeType.valueOf(typeString)
            } catch (e: IllegalArgumentException) {
                return migrateShape(json)
            }

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

                ShapeType.ARCH -> {
                    val radius = json["radius"].toString().toInt()
                    val width = json["width"].toString().toInt()
                    val rotation = Rotation.fromString(json["rotation"].toString())

                    Arch(name, color, pos, radius, width).apply {
                        this.rotation = rotation
                        this.enabled = enabled
                    }
                }

                ShapeType.POLYGON -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val sides = json["sides"].toString().toInt()

                    Polygon(name, color, pos, radius, height, sides).apply {
                        this.enabled = enabled
                    }
                }
            }
        }

        fun getIcon(type: ShapeType) = when (type) {
            ShapeType.CUBE -> Icons.CUBE_ICON
            ShapeType.SPHERE -> Icons.SPHERE_ICON
            ShapeType.CYLINDER -> Icons.CYLINDER_ICON
            ShapeType.ARCH -> Icons.ARCH_ICON
            ShapeType.POLYGON -> Icons.HEXAGON_ICON
        }

        // Use this function to migrate shapes from older versions of the mod
        private fun migrateShape(json: JsonObject): Shape {
            when (val typeString = json["type"].toString().replace("\"", "")) {
                "HEXAGON" -> {
                    val name = json["name"].toString().replace("\"", "")
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val enabled = json["enabled"].toString().toBoolean()
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val rotation = Rotation.fromString(json["rotation"].toString())

                    return Polygon(name, color, pos, radius, height, 6).apply {
                        this.rotation = rotation
                        this.enabled = enabled
                    }
                }

                "TUNNEL" -> {
                    val name = json["name"].toString().replace("\"", "")
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val enabled = json["enabled"].toString().toBoolean()
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val rotation = Rotation.fromString(json["rotation"].toString())
                    rotation.x = 90 // The tunnel is a cylinder rotated 90 degrees, that's why im removing it in 2.0

                    return Cylinder(name, color, pos, radius, height).apply {
                        this.rotation = rotation
                        this.enabled = enabled
                    }
                }

                else -> {
                    throw IllegalArgumentException("Unknown shape type: $typeString")
                }
            }
        }
    }
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, ARCH, POLYGON
}
