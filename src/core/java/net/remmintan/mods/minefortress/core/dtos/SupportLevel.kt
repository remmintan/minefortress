package net.remmintan.mods.minefortress.core.dtos

enum class SupportLevel(val patron: Boolean = false) {
    NO_SUPPORT,
    ZOMBIE(true),
    ZOMBIE_VILLAGER(true),
    VILLAGER(true),
    ERROR,
    NONE
}