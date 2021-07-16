package me.dkim19375.bedwars.plugin.config

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import me.dkim19375.bedwars.plugin.gui.MainShopGUI
import me.dkim19375.dkimbukkitcore.config.ConfigFile
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration

@Suppress("MemberVisibilityCanBePrivate")
class ConfigManager(private val plugin: BedwarsPlugin) {
    private val config: FileConfiguration
        get() = plugin.config
    private val shopFile: ConfigFile
        get() = plugin.shopFile
    private val shopConfig: FileConfiguration
        get() = shopFile.config
    var mainItems = emptySet<MainShopConfigItem>()
        private set
    val useMainLobby: Boolean
        get() = config.getBoolean("use-main-lobby")

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