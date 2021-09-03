package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

private val plugin: BedwarsPlugin = JavaPlugin.getPlugin(BedwarsPlugin::class.java)

data class MainDataFile(
    var quickBuySlots: MutableMap<UUID, MutableMap<Int, MainShopConfigItem>> = mutableMapOf(),
    var lobby: Location? = null,
    var editing: MutableSet<String> = mutableSetOf()
) {
    fun save() {
        plugin.mainDataFile.set(this)
        plugin.mainDataFile.save()
    }
}