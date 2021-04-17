package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageByEntityListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun EntityDamageByEntityEvent.onDamage() {
        val entity = entity
        val damager = damager
        if (entity.type != EntityType.PLAYER) {
            return
        }
        if (damager.type != EntityType.PRIMED_TNT && damager.type != EntityType.FIREBALL) {
            return
        }
        val entities = plugin.gameManager.explosives.keys
        if (damager.uniqueId !in entities) {
            return
        }
        damage = finalDamage * 0.2
        entities.remove(damager.uniqueId)
    }
}