package me.dkim19375.bedwars.plugin.enumclass

import me.dkim19375.bedwars.plugin.util.Delay
import org.bukkit.Material

@Suppress("MemberVisibilityCanBePrivate")
enum class SpawnerType(val material: Material, val maxAmount: Int, val delayFirst: Delay, val delaySecond: Delay = delayFirst, val delayThird: Delay = delaySecond) {
    IRON(Material.IRON_INGOT, 48, Delay.fromSeconds(2)),
    GOLD(Material.GOLD_INGOT, 12, Delay.fromSeconds(7)),
    DIAMOND(Material.DIAMOND, 4, Delay.fromSeconds(30), Delay.fromSeconds(23), Delay.fromSeconds(18)),
    EMERALD(Material.EMERALD, 2, Delay.fromSeconds(65), Delay.fromSeconds(50), Delay.fromSeconds(40));

    fun getDelay(delay: Int) = when (delay) {
        1 -> delayFirst
        2 -> delaySecond
        else -> delayThird
    }

    companion object {
        fun fromString(str: String?): SpawnerType? {
            str ?: return null
            return try {
                valueOf(str.toUpperCase())
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
    }
}