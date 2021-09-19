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

import me.dkim19375.bedwars.api.enumclass.SpecialItemType
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.util.enumValueOfOrNull
import me.dkim19375.bedwars.plugin.util.getIntOrNull
import me.dkim19375.dkimbukkitcore.function.logInfo
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import java.util.logging.Level

data class MainShopConfigItem(
    val name: String,
    val slot: Int,
    val item: ItemWrapper,
    val cost: Int,
    val costItem: MainShopGUI.CostType,
    val itemCategory: MainShopGUI.ItemType,
    val displayname: String? = null,
    val permanent: Boolean = false,
    val defaultOnSpawn: Boolean = false,
    val downgrade: () -> MainShopConfigItem? = { null },
    val commands: List<String> = emptyList(),
    val cosmetic: Boolean = false,
    val specialItem: SpecialItemType? = null
) {
    companion object {
        private fun logError(config: ConfigurationSection, section: String, reason: String = "does not exist!") {
            logInfo("Section ${config.name}.$section $reason", Level.SEVERE)
        }

        fun deserialize(config: ConfigurationSection, plugin: BedwarsPlugin): MainShopConfigItem? {
            val row = config.getIntOrNull("row")
            val column = config.getIntOrNull("column")
            val slot = config.getIntOrNull("slot") ?: run {
                row ?: run {
                    logError(config, "row")
                    return@deserialize null
                }
                column ?: run {
                    logError(config, "row")
                    return@deserialize null
                }
                (column + (row - 1) * 9) - 1
            }
            val itemWrapper = ItemWrapper.fromConfig(config) ?: return null
            val costAmount = config.getInt("cost", 1)
            val costType = enumValueOfOrNull(config.getString("cost-item")) ?: MainShopGUI.CostType.IRON
            val name = config.getString("name")
            val permanent = config.getBoolean("permanent")
            val defaultOnSpawn = config.getBoolean("default-on-spawn")
            val type = enumValueOfOrNull<MainShopGUI.ItemType>(config.getString("item-category")) ?: run {
                logError(
                    config,
                    "item-category",
                    if (config.isSet("item-category")) "- Invalid item-category! Valid: (${
                        MainShopGUI.ItemType.values().map(MainShopGUI.ItemType::name).joinToString()
                    })" else "does not exist!"
                )
                return null
            }
            val downgrade = config.getString("downgrade")
            val commands = config.getStringList("commands")
            val cosmetic = config.getBoolean("cosmetic")
            val specialItem = SpecialItemType.fromString(config.getString("special-type"))
            return MainShopConfigItem(
                name = config.name,
                slot = slot,
                item = itemWrapper,
                cost = costAmount,
                costItem = costType,
                itemCategory = type,
                displayname = name,
                permanent = permanent,
                defaultOnSpawn = defaultOnSpawn,
                downgrade = { downgrade?.let(plugin.shopConfigManager::getItemFromName) },
                commands = commands,
                cosmetic = cosmetic,
                specialItem = specialItem
            )
        }
    }

    fun getFriendlyName(): String = displayname ?: ChatColor.stripColor(item.name) ?: name
}