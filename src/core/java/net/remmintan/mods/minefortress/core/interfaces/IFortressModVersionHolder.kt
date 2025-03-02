package net.remmintan.mods.minefortress.core.interfaces

interface IFortressModVersionHolder {

    fun set_FortressModVersion(version: Int)
    fun is_OutdatedVersion(): Boolean

}