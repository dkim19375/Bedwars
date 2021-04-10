package me.dkim19375.bedwars.plugin.enumclass

import org.bukkit.Material

@Suppress("MemberVisibilityCanBePrivate")
enum class SpawnerType(val material: Material, val delayFirst: Long, val delaySecond: Long = delayFirst, val delayThird: Long = delaySecond) {
    IRON(Material.IRON_INGOT, 40L),
    GOLD(Material.GOLD_INGOT, 140L),
    DIAMOND(Material.DIAMOND, 600L, 460L, 360L),
    EMERALD(Material.EMERALD, 1300L, 1000L, 800L);

    companion object {
        fun fromString(str: String?): Team? {
            str ?: return null
            return try {
                Team.valueOf(str)
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}