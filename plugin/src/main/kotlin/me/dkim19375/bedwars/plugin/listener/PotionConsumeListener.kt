package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.potion.Potion
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType

class PotionConsumeListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private fun PlayerItemConsumeEvent.onEvent() {
        if (item.type != Material.POTION) {
            return
        }
        val potion = Potion.fromItemStack(item) ?: return
        val potionType = potion.type ?: return
        val type = potionType.effectType
        plugin.gameManager.getGame(player) ?: return
        if (potionType == PotionType.INVISIBILITY) {
            player.addPotionEffect(PotionEffect(type, 600, 0), true)
            plugin.packetManager.hideArmor(player)
            return
        }
        if (type == PotionEffectType.SPEED) {
            player.addPotionEffect(PotionEffect(type, 900, 1), true)
            return
        }
        if (type == PotionEffectType.JUMP) {
            player.addPotionEffect(PotionEffect(type, 900, 4), true)
            return
        }
    }
}