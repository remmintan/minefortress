package net.remmintan.mods.minefortress.core.config // Or your preferred config package

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.fabricmc.loader.api.FabricLoader
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Path

object MineFortressClientConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val configFile: Path = FabricLoader.getInstance().configDir.resolve("minefortress-client.json")

    private const val DISCLAIMER_ACKNOWLEDGED_KEY = "disclaimerAcknowledged"

    // Holds the loaded state in memory
    var disclaimerAcknowledged: Boolean = false
        private set // Only allow modification through load/save logic

    fun load() {
        disclaimerAcknowledged = false // Default to false
        if (configFile.toFile().exists()) {
            try {
                FileReader(configFile.toFile()).use { reader ->
                    val json = JsonParser.parseReader(reader).asJsonObject
                    if (json.has(DISCLAIMER_ACKNOWLEDGED_KEY)) {
                        disclaimerAcknowledged = json.get(DISCLAIMER_ACKNOWLEDGED_KEY).asBoolean
                    }
                }
            } catch (e: Exception) {
                System.err.println("[MineFortress] Failed to load client config: ${e.message}")
                // Keep disclaimerAcknowledged as false if loading fails
            }
        }
        println("[MineFortress] Loaded client config. Disclaimer acknowledged: $disclaimerAcknowledged")
    }

    fun acknowledgeDisclaimer() {
        if (!disclaimerAcknowledged) {
            disclaimerAcknowledged = true
            save()
            println("[MineFortress] Disclaimer acknowledged and saved.")
        }
    }

    private fun save() {
        try {
            FileWriter(configFile.toFile()).use { writer ->
                val json = JsonObject()
                json.addProperty(DISCLAIMER_ACKNOWLEDGED_KEY, disclaimerAcknowledged)
                gson.toJson(json, writer)
            }
        } catch (e: Exception) {
            System.err.println("[MineFortress] Failed to save client config: ${e.message}")
        }
    }
}