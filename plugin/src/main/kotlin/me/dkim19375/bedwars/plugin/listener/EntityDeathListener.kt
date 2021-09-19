package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.util.isDropsDisabled
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityDeathListener : Listener {
    @EventHandler(priority = EventPriority.HIGH)
    private fun EntityDeathEvent.onDeath() {
        if (entity is Player) {
            return
        }
        if (!entity.isDropsDisabled().first) {
            return
        }
        droppedExp = 0
        drops.clear()
    }
}