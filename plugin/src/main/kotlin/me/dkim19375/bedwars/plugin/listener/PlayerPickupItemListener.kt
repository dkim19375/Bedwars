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

package me.dkim19375.bedwars.plugin.listener

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.NEW_SOUND
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.giveItem
import me.dkim19375.bedwars.plugin.util.isDrop
import me.dkim19375.bedwars.plugin.util.setDrop
import me.dkim19375.dkimbukkitcore.function.getPlayers
import me.dkim19375.dkimbukkitcore.function.playSound
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent
import java.util.*
import kotlin.random.Random.Default.nextDouble

class PlayerPickupItemListener(private val plugin: BedwarsPlugin) : Listener {
    private val collected = mutableSetOf<UUID>()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private fun PlayerPickupItemEvent.onPickup() {
        plugin.gameManager.getGame(player) ?: return
        if (collected.contains(item.uniqueId)) {
            isCancelled = true
            return
        }
        item.pickupDelay = Int.MAX_VALUE
        val itemStack = item.itemStack.clone()
        val isDrop = item.isDrop().let {
            it.second.remove()
            it.first
        }
        item.setDrop(false).remove()
        isCancelled = true
        collected.add(item.uniqueId)
        Bukkit.getScheduler().runTask(plugin) {
            collected.remove(item.uniqueId)
            item.remove()
        }
        val generators =
            plugin.mainConfigManager.get(MainConfigSettings.SPLIT_GENERATORS).mapNotNull(SpawnerType::fromString)
                .toSet()
        val generator = SpawnerType.fromMaterial(itemStack.type)
        val enabled = plugin.mainConfigManager.get(MainConfigSettings.SPLIT_ENABLED)
                && generator != null
                && generators.contains(generator)
        val players = (if (enabled && isDrop) {
            item.getNearbyEntities(1.7, 2.0, 1.7).mapNotNull { (it as? Player)?.uniqueId }
        } else {
            emptyList()
        }).plus(player.uniqueId).toSet().getPlayers()
        val sound = if (NEW_SOUND) Sound.valueOf("ENTITY_ITEM_PICKUP") else Sound.ITEM_PICKUP
        for (loopPlayer in players) {
            loopPlayer.giveItem(itemStack.clone())
            loopPlayer.playSound(sound, 0.2f, nextDouble(1.0, 2.5).coerceAtMost(2.0).toFloat())
            plugin.packetManager.collectItem(item, loopPlayer)
        }
    }
}