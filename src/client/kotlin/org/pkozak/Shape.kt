package org.pkozak

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonObject
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.shape.Cube
import org.pkozak.shape.Cylinder
import org.pkozak.shape.Sphere
import java.awt.Color

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    abstract val name: String
    abstract val type: ShapeType
    var filled = false
    var enabled = true

    abstract fun render(): MutableSet<Vec3d>

    fun toggleVisibility() {
        enabled = !enabled
    }

    fun getIcon() = when (type) {
        ShapeType.CUBE -> PhantomShapesClient.CUBE_ICON
        ShapeType.SPHERE -> PhantomShapesClient.SPHERE_ICON
        ShapeType.CYLINDER -> PhantomShapesClient.CYLINDER_ICON
        else -> throw IllegalArgumentException("Unsupported shape type")
    }

    abstract fun toJsonObject(): JsonObject

    companion object {
        fun fromJsonObject(json: JsonObject): Shape {
            val type = ShapeType.valueOf(
                json["type"].toString().replace("\"", "")
            ) // For some reason, the type is wrapped in quotes
            val name = json["name"].toString().replace("\"", "") // Same here

            return when (type) {
                ShapeType.CUBE -> {
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val dimensions = Vec3i(
                        (json["dimensions"] as JsonObject)["x"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["y"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["z"].toString().toInt()
                    )
                    val filled = json["filled"].toString().toBoolean()

                    Cube(name, color, pos, dimensions).apply {
                        this.filled = filled
                    }
                }

                ShapeType.SPHERE -> {
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val radius = json["radius"].toString().toInt()
                    val filled = json["filled"].toString().toBoolean()

                    Sphere(name, color, pos, radius).apply {
                        this.filled = filled
                    }
                }

                ShapeType.CYLINDER -> {
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val filled = json["filled"].toString().toBoolean()

                    Cylinder(name, color, pos, radius, height).apply {
                        this.filled = filled
                    }
                }

                ShapeType.TUNNEL -> TODO()
                ShapeType.CONE -> TODO()
                ShapeType.PYRAMID -> TODO()
            }
        }

        fun getIcon(type: ShapeType) = when (type) {
            ShapeType.CUBE -> PhantomShapesClient.CUBE_ICON
            ShapeType.SPHERE -> PhantomShapesClient.SPHERE_ICON
            ShapeType.CYLINDER -> PhantomShapesClient.CYLINDER_ICON
            else -> throw IllegalArgumentException("Unsupported shape type")
        }
    }
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, TUNNEL, CONE, PYRAMID;
}
