/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
            item.addUnsafeEnchantment(e, 1)
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