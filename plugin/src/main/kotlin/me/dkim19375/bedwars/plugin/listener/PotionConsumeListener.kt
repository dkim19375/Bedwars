package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionType

class PotionConsumeListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun PlayerItemConsumeEvent.onEvent() {
        if (item.type == Material.POTION && Potion.fromItemStack(item).type == PotionType.INVISIBILITY
        ) {
            val player: Player = player
            plugin.hideArmor(player)
        }
    }
}