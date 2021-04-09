package me.dkim19375.bedwars.plugin.enumclass

import org.bukkit.Material

enum class SpawnerType(material: Material, delay: Long) {
    IRON(Material.IRON_INGOT, 40L),
    GOLD(Material.GOLD_INGOT, 140L),
    DIAMOND(Material.DIAMOND, 600L),
    EMERALD(Material.EMERALD, 1300L)
}