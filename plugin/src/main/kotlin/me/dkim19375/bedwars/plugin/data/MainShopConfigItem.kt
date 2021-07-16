package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.bedwars.plugin.util.enumValueOfOrNull
import me.dkim19375.bedwars.plugin.util.getIntOrNull
import me.dkim19375.dkimbukkitcore.function.logInfo
import org.bukkit.configuration.ConfigurationSection
import java.util.logging.Level

data class MainShopConfigItem(
    val name: String, val slot: Int, val item: ItemWrapper, val cost: Int, val costItem: MainShopGUI.CostType,
    val displayname: String?, val permanent: Boolean = false, val defaultOnSpawn: Boolean = false,
    val itemCategory: MainShopGUI.ItemType
) {
    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MainShopConfigItem

        if (slot != other.slot) return false
        if (item != other.item) return false
        if (cost != other.cost) return false
        if (costItem != other.costItem) return false
        if (displayname != other.displayname) return false
        if (permanent != other.permanent) return false
        if (defaultOnSpawn != other.defaultOnSpawn) return false
        if (itemCategory != other.itemCategory) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slot
        result = 31 * result + item.hashCode()
        result = 31 * result + cost
        result = 31 * result + costItem.hashCode()
        result = 31 * result + (displayname?.hashCode() ?: 0)
        result = 31 * result + permanent.hashCode()
        result = 31 * result + defaultOnSpawn.hashCode()
        result = 31 * result + itemCategory.hashCode()
        return result
    }

    companion object {
        private fun logError(config: ConfigurationSection, section: String, reason: String = "does not exist!") {
            logInfo("Section ${config.name}.$section $reason", Level.SEVERE)
        }

        fun deserialize(config: ConfigurationSection): MainShopConfigItem? {
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
            val costType = enumValueOfOrNull<MainShopGUI.CostType>(config.getString("cost-item")) ?: run {
                logError(
                    config,
                    "cost-item",
                    if (config.isSet("cost-item")) "- Invalid cost-item! Valid: (${
                        MainShopGUI.CostType.values().map(MainShopGUI.CostType::name).joinToString()
                    })" else "does not exist!"
                )
                return null
            }
            val displayname = config.getString("display-name")
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
            return MainShopConfigItem(
                name = config.name,
                slot = slot,
                item = itemWrapper,
                cost = costAmount,
                costItem = costType,
                displayname = displayname,
                permanent = permanent,
                defaultOnSpawn = defaultOnSpawn,
                itemCategory = type
            )
        }
    }
}