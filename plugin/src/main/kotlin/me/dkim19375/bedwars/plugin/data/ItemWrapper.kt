/*
 *     Bedwars, a minigame for spigot
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.util.addAllFlags
import me.dkim19375.bedwars.plugin.util.enumValueOfOrNull
import me.dkim19375.bedwars.plugin.util.setConfigItem
import me.dkim19375.bedwars.plugin.util.setUnbreakable
import me.dkim19375.dkimbukkitcore.function.logInfo
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.material.Colorable
import org.bukkit.material.Wool
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType
import java.util.logging.Level

data class ItemWrapper(
    val material: Material, val amount: Int, val potionType: PotionType? = null, val configItem: String? = null,
    val potionAmplifier: Int = 1, val potionDuration: Int = 0, val enchants: Map<Enchantment, Int> = emptyMap()
) {
    fun toItemStack(color: DyeColor?): ItemStack {
        val plugin = JavaPlugin.getPlugin(BedwarsPlugin::class.java)
        val configManager = plugin.shopConfigManager
        if (potionType != null) {
            val potion = Potion(potionType, if (!(1..2).contains(potionAmplifier - 1)) 2 else (potionAmplifier - 1))
            val item = potion.toItemStack(amount).setConfigItem(configItem)
            enchants.forEach { (enchant, level) ->
                if (enchant.canEnchantItem(item)) {
                    item.addEnchantment(enchant, level)
                }
            }
            item.itemMeta = item.itemMeta?.apply {
                addAllFlags()
            }
            return item
        }
        val type = configManager.getItemFromName(configItem)
        val item: ItemStack = if (material == Material.WOOL && color != null) {
            Wool(color).toItemStack(amount)
        } else {
            when (ItemStack(material, amount).itemMeta) {
                is LeatherArmorMeta -> ItemStack(material, amount)
                is Colorable -> ItemStack(material, amount)
                else -> {
                    @Suppress("LiftReturnOrAssignment") // fix warning = error?!
                    if (color == null) {
                        ItemStack(material, amount)
                    } else {
                        if (type != null && type.itemCategory == MainShopGUI.ItemType.BLOCKS) {
                            @Suppress("DEPRECATION")
                            ItemStack(material, amount, color.data.toShort())
                        } else {
                            ItemStack(material, amount)
                        }
                    }
                }
            }
        }.setConfigItem(configItem).setUnbreakable(true)
        enchants.forEach { (enchant, level) ->
            item.addUnsafeEnchantment(enchant, level)
        }
        item.itemMeta = item.itemMeta?.apply {
            if (this is LeatherArmorMeta && color != null) {
                this.color = color.color
            }
            addAllFlags()
        }
        return item
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemWrapper

        if (material != other.material) return false
        if (amount != other.amount) return false
        if (potionType != other.potionType) return false
        if (potionAmplifier != other.potionAmplifier) return false
        if (enchants != other.enchants) return false

        return true
    }

    override fun hashCode(): Int {
        var result = material.hashCode()
        result = 31 * result + amount
        result = 31 * result + (potionType?.hashCode() ?: 0)
        result = 31 * result + potionAmplifier
        result = 31 * result + enchants.hashCode()
        return result
    }


    companion object {
        @Suppress("SameParameterValue")
        private fun logError(config: ConfigurationSection, section: String, reason: String = "does not exist!") {
            logInfo("Section ${config.name}.$section $reason", Level.SEVERE)
        }

        fun fromConfig(config: ConfigurationSection): ItemWrapper? {
            val material = Material.matchMaterial(config.getString("material")?.uppercase() ?: ".") ?: run {
                logError(
                    config,
                    "material",
                    if (config.isSet("material")) "- Invalid material! Valid: (${
                        Material.values().map(Material::name).joinToString()
                    })" else "does not exist!"
                )
                return null
            }
            val amount = config.getInt("amount", 1)
            val potionType = enumValueOfOrNull<PotionType>(config.getString("potion-type"))
            val potionAmplifier = config.getInt("potion-amplifier", 1)
            val potionDuration = config.getInt("potion-duration", 0)
            val enchants = config.getStringList("enchants").mapNotNull { str ->
                val split = str.split(':')
                if (split.isEmpty()) {
                    return@mapNotNull null
                }
                Enchantment.getByName(split[0].uppercase()) to (split.getOrNull(1)?.toIntOrNull() ?: 1)
            }.toMap()
            return ItemWrapper(material, amount, potionType, config.name, potionAmplifier, potionDuration, enchants)
        }
    }
}