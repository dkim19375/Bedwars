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

package me.dkim19375.bedwars.plugin.config

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.util.getConfigItem
import me.dkim19375.dkimbukkitcore.config.SpigotConfigFile
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

@Suppress("MemberVisibilityCanBePrivate")
class ShopConfigManager(private val plugin: BedwarsPlugin) {
    private val shopFile: SpigotConfigFile
        get() = plugin.shopFile
    private val shopConfig: FileConfiguration
        get() = shopFile.config
    val mainItems = mutableSetOf<MainShopConfigItem>()
    val weights = mutableMapOf<String, MutableMap<MainShopConfigItem, Int>>()

    fun update() {
        mainItems.clear()
        mainItems.addAll(shopConfig.getKeys(false)
            .mapNotNull(shopConfig::getConfigurationSection)
            .mapNotNull { MainShopConfigItem.deserialize(it, plugin) }
            .toSet())
        weights.clear()
        for (item in mainItems) {
            val weight = item.weight
            val category = item.weightCategory ?: continue
            weights.getOrPut(category, ::mutableMapOf)[item] = weight
        }
    }

    fun getItemFromMaterial(material: Material): MainShopConfigItem? =
        mainItems.firstOrNull { i -> i.item.material == material }

    fun getItemFromName(name: String?): MainShopConfigItem? = name?.let { mainItems.firstOrNull { it.name == name } }

    fun getByType(type: MainShopGUI.ItemType): Set<MainShopConfigItem> {
        val set = mutableSetOf<MainShopConfigItem>()
        mainItems.forEach { i ->
            if (i.itemCategory == type) {
                set.add(i)
            }
        }
        return set.toSet()
    }

    fun isHigherWeight(lower: MainShopConfigItem, higher: MainShopConfigItem): Boolean {
        var category: String? = null
        var weight: Int? = null
        for ((mapCategory, map) in weights) {
            val mapWeight = map[lower] ?: continue
            category = mapCategory
            weight = mapWeight
            break
        }
        category ?: return true
        weight ?: return true
        val higherWeight = weights[category]?.get(higher) ?: return true
        return higherWeight >= weight
    }

    fun getWeightInfo(item: MainShopConfigItem): Pair<String, Int>? = weights.toList().mapNotNull { (category, map) ->
        val weight = map[item] ?: return@mapNotNull null
        category to weight
    }.firstOrNull()

    fun canGetItem(inv: PlayerInventory, item: MainShopConfigItem): Boolean = inv.filterNotNull()
        .mapNotNull(ItemStack::getConfigItem)
        .none { invItem ->
            isHigherWeight(item, invItem)
        }

    fun getOtherItemsWithWeight(inv: PlayerInventory, item: MainShopConfigItem): Set<ItemStack> {
        val weightInfo = getWeightInfo(item) ?: return emptySet()
        return inv.filter {
            val info = it?.getConfigItem()?.let(this@ShopConfigManager::getWeightInfo) ?: return@filter false
            info.first == weightInfo.first
        }.toSet()
    }
}