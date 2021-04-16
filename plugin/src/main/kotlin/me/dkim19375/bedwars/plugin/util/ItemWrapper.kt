package me.dkim19375.bedwars.plugin.util

import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.material.Colorable
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType

data class ItemWrapper (
    val material: Material, val amount: Int, val potionType: PotionType? = null,
    val potionAmplifier: Int = 1, val enchants: List<Enchantment> = listOf()
) {
    fun toItemStack(): ItemStack = toItemStack(null)

    fun toItemStack(color: DyeColor?): ItemStack {
        if (potionType == null) {
            val item: ItemStack
            if (ItemStack(material, amount).itemMeta is Colorable) {
                item = ItemStack(material, amount)
            } else {
                @Suppress("DEPRECATION", "LiftReturnOrAssignment") // fix warning = error?!
                if (color == null) {
                    item = ItemStack(material, amount)
                } else {
                    item = ItemStack(material, amount, color.data.toShort())
                }
            }
            enchants.forEach { e -> item.addEnchantment(e, 1) }
            item.itemMeta?.let {
                val meta = it as Colorable
                meta.color = color
                item.itemMeta = meta as ItemMeta
            }
            return item
        }
        val potion = Potion(potionType, potionAmplifier - 1)
        potion.setHasExtendedDuration(false)
        val item = potion.toItemStack(amount)
        enchants.forEach { e -> item.addEnchantment(e, 1) }
        return item
    }
}