package org.minefortress.earlyriser

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.fabricmc.loader.impl.discovery.ModResolutionException

class MineFortressPreLaunch : PreLaunchEntrypoint {

    // Define constants for better readability and maintenance
    private val THIS_MOD_ID = "minefortress"
    private val REQUIRED_MOD_ID = "keybind_fix"
    private val REQUIRED_MOD_VERSION = ">=1.0.0" // From your mod.json

    override fun onPreLaunch() {
        // This check ensures the code only runs on the client
        if (FabricLoader.getInstance().environmentType == EnvType.CLIENT) {

            // Check if the required client-side mod is loaded
            if (!FabricLoader.getInstance().isModLoaded(REQUIRED_MOD_ID)) {

                // Get our own mod's metadata to make the error message more specific
                val modContainer = FabricLoader.getInstance()
                    .getModContainer(THIS_MOD_ID)
                    .orElseThrow { IllegalStateException("Could not find the '$THIS_MOD_ID' mod container, which is required for a dependency check.") }

                val metadata = modContainer.metadata
                val modName = metadata.name
                val modVersion = metadata.version.friendlyString

                // Build a clear, multi-line error message
                val errorMessage = """
                    |
                    |The mod '$modName' ($THIS_MOD_ID) version $modVersion has a missing dependency.
                    |
                    |It requires the mod 'Keybind Fix' ($REQUIRED_MOD_ID) version $REQUIRED_MOD_VERSION to be installed on the client.
                    |
                    |Please download and install the required mod to continue.
                    """.trimMargin() // trimMargin() cleans up the indentation in multi-line strings

                // Throw the special exception to show Fabric's error screen
                throw ModResolutionException(errorMessage)
            }
        }
    }
}