package org.pkozak

import kotlinx.serialization.json.JsonObject
import net.minecraft.util.math.Vec3d
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
    var drawOnlyEdge = false

    abstract fun render(): MutableSet<Vec3d>

    fun toggleVisibility() {
        enabled = !enabled
    }

    abstract fun toJsonObject(): JsonObject

    companion object {
        fun fromJsonObject(json: JsonObject): Shape {
            return when (ShapeType.valueOf(json["type"].toString())) {
                ShapeType.CUBE -> {
                    val name = json["name"].toString()
                    val color = Color(json["color"].toString().toInt())
                    val pos = Vec3d(
                        (json["pos"] as JsonObject)["x"].toString().toDouble(),
                        (json["pos"] as JsonObject)["y"].toString().toDouble(),
                        (json["pos"] as JsonObject)["z"].toString().toDouble()
                    )
                    val dimensions = Vec3d(
                        (json["dimensions"] as JsonObject)["x"].toString().toDouble(),
                        (json["dimensions"] as JsonObject)["y"].toString().toDouble(),
                        (json["dimensions"] as JsonObject)["z"].toString().toDouble()
                    )
                    val filled = json["filled"].toString().toBoolean()

                    Cube(name, color, pos, dimensions).apply {
                        this.filled = filled
                    }
                }

                ShapeType.SPHERE -> {
                    val name = json["name"].toString()
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
                    val name = json["name"].toString()
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

    }
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, TUNNEL, CONE, PYRAMID;
}
