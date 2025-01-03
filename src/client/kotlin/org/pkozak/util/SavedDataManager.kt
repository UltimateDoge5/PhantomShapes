package org.pkozak.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.WorldSavePath
import org.pkozak.PhantomShapesClient
import org.pkozak.PhantomShapesClient.logger
import org.pkozak.shape.Shape
import java.io.*


class SavedDataManager {
    companion object {
        fun loadShapes(): MutableList<Shape> {
            val client = MinecraftClient.getInstance()
            val shapes = mutableListOf<Shape>()

            val fileName: String

            // Single player is a local server
            if (client.isInSingleplayer) {
                val saveName = client.server?.getSavePath(WorldSavePath.ROOT)?.parent?.fileName
                if (saveName == null) {
                    logger.error("Failed to load shapes. Save name was null")
                    return shapes
                }

                fileName = saveName.toString()
            } else {
                val serverAddress = client.currentServerEntry?.address
                if (serverAddress == null) {
                    logger.error("Failed to load shapes. Server address was null")
                    return shapes
                }

                fileName = serverAddress
            }

            val jsonString = readFromFile("$fileName.json")
            if (jsonString != null) {
                val jsonArray = Json.decodeFromString(JsonArray.serializer(), jsonString)
                jsonArray.forEach {
                    try {
                        shapes.add(Shape.fromJsonObject(it.jsonObject))
                    } catch (e: Exception) {
                        logger.error("Shape ${jsonArray.indexOf(it)} out of ${jsonArray.size - 1} is of incorrect format, some data might be missing")
                        PhantomShapesClient.overwriteProtection = true
                    }
                }
            }

            return shapes
        }

        fun readFromFile(s: String, configDir: Boolean = false): String? {
            val dir = FabricLoader.getInstance().configDir.resolve(if (configDir) "" else "shapes")
            val file = File(dir.toFile(), s)

            if (!file.exists()) {
                return null
            }

            return try {
                val inputStream: InputStream = FileInputStream(file)
                val text = inputStream.bufferedReader().use { it.readText() }
                text
            } catch (e: IOException) {
                logger.error("Failed to read from file: $s")
                logger.error(e.message)
                null
            }
        }

        fun saveShapes(shapes: List<Shape>) {
            val client = MinecraftClient.getInstance()
            val jsonString = JsonArray(shapes.map { it.toJsonObject() }).toString()

            // Single player is a local server
            if (client.isInSingleplayer) {
                val saveName = client.server?.getSavePath(WorldSavePath.ROOT)?.parent?.fileName
                if (saveName == null) {
                    logger.error("Failed to save shapes. Save name was null")
                    return
                }

                writeToFile("$saveName.json", jsonString)
            } else {
                val serverAddress = client.currentServerEntry?.address
                if (serverAddress == null) {
                    logger.error("Failed to save shapes. Server address was null")
                    return
                }

                writeToFile("$serverAddress.json", jsonString)
            }

            logger.info("Successfully saved shapes to file")
        }

        fun writeToFile(filename: String, jsonString: String, configDir: Boolean = false): Boolean {
            val dir = FabricLoader.getInstance().configDir.resolve(if (configDir) "" else "shapes")
            val file = File(dir.toFile(), filename)

            // Check if the mod directory exists
            if (!dir.toFile().exists()) {
                if (!dir.toFile().mkdirs()) {
                    logger.error("Failed to create directory: $dir")
                    return false
                }
            }

            try {
                val outputStream: OutputStream = FileOutputStream(file)
                outputStream.write(jsonString.toByteArray())
                outputStream.close()
                return true
            } catch (e: IOException) {
                logger.error("Failed to write to file: $filename")
                logger.error(e.message)
            }

            return false
        }

        fun toSafeFloat(obj: JsonObject, key: String, fallback: Float): Float {
            return try {
                obj[key].toString().toFloat()
            } catch (e: Exception) {
                logger.warn("Failed to parse float from key: $key. Using fallback value")
                fallback
            }
        }

        fun toSafeBoolean(obj: JsonObject, key: String, fallback: Boolean): Boolean {
            return try {
                obj[key].toString().toBoolean()
            } catch (e: Exception) {
                logger.warn("Failed to parse boolean from key: $key. Using fallback value")
                fallback
            }
        }
    }
}