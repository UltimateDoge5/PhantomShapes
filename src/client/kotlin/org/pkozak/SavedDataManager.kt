package org.pkozak

import kotlinx.serialization.json.JsonArray
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.WorldSavePath
import org.pkozak.PhantomShapesClient.logger
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class SavedDataManager {
    companion object {
        fun loadShapes(): List<Shape> {
            // TODO
            return emptyList()
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
        }

        private fun writeToFile(filename: String, jsonString: String): Boolean {
            val dir = FabricLoader.getInstance().configDir.resolve("shapes")
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
    }
}