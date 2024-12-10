package org.minefortress.blueprints.manager

import net.minecraft.client.MinecraftClient
import net.remmintan.mods.minefortress.core.interfaces.blueprints.IBlueprintsImportExportManager
import org.minefortress.renderer.gui.blueprints.ImportExportBlueprintsScreen
import org.minefortress.utils.ModUtils
import java.io.IOException
import java.nio.file.Files

class BlueprintsImportExportManager : IBlueprintsImportExportManager {

    override fun handleBlueprintsImport() {
        this.success()
    }

    override fun handleBlueprintsExport(name: String?, bytes: ByteArray?) {
        val blueprintsFolder = ModUtils.getBlueprintsFolder()
        if (!blueprintsFolder.toFile().exists()) {
            blueprintsFolder.toFile().mkdirs()
        }
        val path = blueprintsFolder.resolve(name)
        val file = path.toFile()
        if (!file.exists()) {
            try {
                file.createNewFile()
                Files.write(path, bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                this.handleImportExportFailure()
                return
            }
        }

        this.success()
    }

    override fun handleImportExportFailure() {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is ImportExportBlueprintsScreen) {
            currentScreen.fail()
        }
    }

    private fun success() {
        val currentScreen = MinecraftClient.getInstance().currentScreen
        if (currentScreen is ImportExportBlueprintsScreen) {
            currentScreen.success()
        }
    }


}