package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.util.isTool
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent

class PlayerItemDamageListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler
    fun PlayerItemDamageEvent.onDamage() {
        plugin.gameManager.getGame(player)?: return
        if (item.type.isTool()) {
            isCancelled = true
        }
    }
}