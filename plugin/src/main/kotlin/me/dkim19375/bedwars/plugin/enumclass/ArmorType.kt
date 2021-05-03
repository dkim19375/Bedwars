package me.dkim19375.bedwars.plugin.enumclass

import org.bukkit.Material

@Suppress("unused")
enum class ArmorType(val boots: Material, val leggings: Material) {
    LEATHER(Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS),
    CHAIN(Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS),
    IRON(Material.IRON_BOOTS, Material.IRON_LEGGINGS),
    DIAMOND(Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS);

    companion object {
        fun fromMaterial(mat: Material?): ArmorType? {
            for (type in values()) {
                if (listOf(type.boots, type.leggings).contains(mat)) {
                    return type
                }
            }
            return null
        }
    }
}