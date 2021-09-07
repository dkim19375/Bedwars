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
import me.dkim19375.dkimbukkitcore.function.getPlayers
import me.dkim19375.dkimbukkitcore.function.playSound
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.ProjectileLaunchEvent

class ProjectileLaunchListener(private val plugin: BedwarsPlugin) : Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun ProjectileLaunchEvent.onLaunch() {
        if (entity.type != EntityType.ENDER_PEARL) {
            return
        }
        val shooter = entity.shooter as? Player ?: return
        val game = plugin.gameManager.getGame(shooter) ?: return
        game.getPlayersInGame().getPlayers().forEach {
            val sound = if (NEW_SOUND) Sound.valueOf("ENTITY_ENDERMEN_TELEPORT") else Sound.ENDERMAN_TELEPORT
            it.playSound(sound)
        }
    }
}