package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.MainShopItems
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.material.Colorable
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType

data class ItemWrapper (
    val material: Material, val amount: Int, val potionType: PotionType? = null,
    val potionAmplifier: Int = 1, val enchants: List<Enchantment> = listOf()
) {
    fun toItemStack(color: DyeColor?): ItemStack {
        if (potionType != null) {
            val potion = Potion(potionType, if (!(1..2).contains(potionAmplifier - 1)) 2 else (potionAmplifier - 1))
            val item = potion.toItemStack(amount)
            enchants.forEach { e ->
                if (e.canEnchantItem(item)) {
                    item.addEnchantment(e, 1)
                }
            }
            item.itemMeta?.let { meta ->
                meta.addItemFlags(*ItemFlag.values())
                item.itemMeta = meta
            }
            return item
        }
        val item: ItemStack
        if (ItemStack(material, amount).itemMeta is Colorable) {
            item = ItemStack(material, amount)
        } else {
            @Suppress("DEPRECATION", "LiftReturnOrAssignment") // fix warning = error?!
            if (color == null) {
                item = ItemStack(material, amount)
            } else {
                val type = MainShopItems.getByMaterial(material)
                if (type != null && type.type == MainShopGUI.ItemType.BLOCKS) {
                    item = ItemStack(material, amount, color.data.toShort())
                } else {
                    item = ItemStack(material, amount)
                }
            }
        }
        enchants.forEach { e ->
            if (e.canEnchantItem(item)) {
                item.addEnchantment(e, 1)
            }
        }
        item.itemMeta?.let {
            if (it is Colorable) {
                it.color = color
            }
            it.addItemFlags(*ItemFlag.values())
            item.itemMeta = it
        }
        return item
    }
}