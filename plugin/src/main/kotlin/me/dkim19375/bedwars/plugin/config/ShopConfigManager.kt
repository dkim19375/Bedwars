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
import me.dkim19375.dkimbukkitcore.config.ConfigFile
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration

@Suppress("MemberVisibilityCanBePrivate")
class ShopConfigManager(private val plugin: BedwarsPlugin) {
    private val shopFile: ConfigFile
        get() = plugin.shopFile
    private val shopConfig: FileConfiguration
        get() = shopFile.config
    var mainItems = emptySet<MainShopConfigItem>()
        private set

    fun update() {
        mainItems = shopConfig.getKeys(false)
            .mapNotNull(shopConfig::getConfigurationSection)
            .mapNotNull(MainShopConfigItem::deserialize)
            .toSet()
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
}