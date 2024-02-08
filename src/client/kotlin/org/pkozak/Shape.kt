package org.pkozak

import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonObject
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.shape.*
import java.awt.Color

abstract class Shape {
    abstract var color: Color
    abstract var pos: Vec3d
    abstract val name: String
    abstract val type: ShapeType
    var enabled = true

    abstract fun render(): MutableSet<Vec3d>

    fun toggleVisibility() {
        enabled = !enabled
    }

    fun getIcon() = when (type) {
        ShapeType.CUBE -> PhantomShapesClient.CUBE_ICON
        ShapeType.SPHERE -> PhantomShapesClient.SPHERE_ICON
        ShapeType.CYLINDER -> PhantomShapesClient.CYLINDER_ICON
        ShapeType.TUNNEL -> PhantomShapesClient.TUNNEL_ICON
        ShapeType.ARCH_BRIDGE -> PhantomShapesClient.ARCH_BRIDGE_ICON
    }

    abstract fun toJsonObject(): JsonObject

    companion object {
        fun fromJsonObject(json: JsonObject): Shape {
            val type = ShapeType.valueOf(
                json["type"].toString().replace("\"", "")
            ) // For some reason, the type is wrapped in quotes
            val name = json["name"].toString().replace("\"", "") // Same here
            val color = Color(json["color"].toString().toInt())
            val pos = Vec3d(
                (json["pos"] as JsonObject)["x"].toString().toDouble(),
                (json["pos"] as JsonObject)["y"].toString().toDouble(),
                (json["pos"] as JsonObject)["z"].toString().toDouble()
            )

            return when (type) {
                ShapeType.CUBE -> {
                    val dimensions = Vec3i(
                        (json["dimensions"] as JsonObject)["x"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["y"].toString().toInt(),
                        (json["dimensions"] as JsonObject)["z"].toString().toInt()
                    )
                    val enabled = json["enabled"].toString().toBoolean()

                    Cube(name, color, pos, dimensions).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.SPHERE -> {
                    val radius = json["radius"].toString().toInt()
                    val enabled = json["enabled"].toString().toBoolean()

                    Sphere(name, color, pos, radius).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.CYLINDER -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()
                    val enabled = json["enabled"].toString().toBoolean()

                    Cylinder(name, color, pos, radius, height).apply {
                        this.enabled = enabled
                    }
                }

                ShapeType.TUNNEL -> {
                    val radius = json["radius"].toString().toInt()
                    val height = json["height"].toString().toInt()

                    Tunnel(name, color, pos, radius, height)
                }

                ShapeType.ARCH_BRIDGE -> {
                    val radius = json["radius"].toString().toInt()
                    val width = json["width"].toString().toInt()

                    ArchBridge(name, color, pos, radius, width)
                }
            }
        }

        fun getIcon(type: ShapeType) = when (type) {
            ShapeType.CUBE -> PhantomShapesClient.CUBE_ICON
            ShapeType.SPHERE -> PhantomShapesClient.SPHERE_ICON
            ShapeType.CYLINDER -> PhantomShapesClient.CYLINDER_ICON
            ShapeType.TUNNEL -> PhantomShapesClient.TUNNEL_ICON
            ShapeType.ARCH_BRIDGE -> PhantomShapesClient.ARCH_BRIDGE_ICON
        }
    }
}

enum class ShapeType {
    CUBE, SPHERE, CYLINDER, TUNNEL, ARCH_BRIDGE;
}
