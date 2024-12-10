package net.remmintan.mods.minefortress.core.interfaces.blueprints

interface IBlueprintsImportExportManager {

    fun handleBlueprintsImport()
    fun handleBlueprintsExport(name: String?, bytes: ByteArray?)
    fun handleImportExportFailure()

}