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

package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.api.enumclass.GameState
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.bedwars.plugin.util.setDrop
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class SpawnerManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val timeFromLast = mutableMapOf<SpawnerType, Long>()
    val upgradeLevels = mutableMapOf<SpawnerType, Int>()

    @Suppress("MemberVisibilityCanBePrivate")
    var runnable: BukkitRunnable? = null
        private set

    fun start() {
        runnable?.cancel()
        runnable = object : BukkitRunnable() {
            override fun run() {
                if (game.state != GameState.STARTED) {
                    return
                }
                val types = mutableSetOf<SpawnerType>()
                val time = System.currentTimeMillis()
                for (type in SpawnerType.values()) {
                    if (getTimeUntilNextDrop(type).millis <= 0) {
                        types.add(type)
                        timeFromLast[type] = time
                    }
                    val second = type.secondTime?.millis ?: continue
                    val third = type.thirdTime?.millis ?: continue
                    if (game.time + third <= time) {
                        upgradeLevels[type] = 3
                        continue
                    }
                    if (game.time + second > time) {
                        continue
                    }
                    upgradeLevels[type] = 2
                }

                val spawners = game.data.spawners
                for (spawner in spawners) {
                    if (!types.contains(spawner.type)) {
                        continue
                    }
                    val loc = Location(Bukkit.getWorld(spawner.location.world.name),
                        spawner.location.x,
                        spawner.location.y,
                        spawner.location.z)
                    val item = loc.world.dropItem(loc, ItemStack(spawner.type.material)).setDrop(true)
                    val entities = item.getNearbyEntities(4.0, 4.0, 4.0)
                    var amount = 1
                    for (entity in entities) {
                        val itemEntity = entity as? Item?: continue
                        if (itemEntity.itemStack.type == item.itemStack.type) {
                            amount += itemEntity.itemStack.amount
                        }
                    }
                    if (amount > spawner.type.maxAmount) {
                        item.remove()
                    }
                }
            }
        }
        runnable?.runTaskTimer(plugin, 5L, 5L)
    }

    fun reset() {
        timeFromLast.clear()
        upgradeLevels.clear()
        runnable?.cancel()
        runnable = null
    }

    fun getTimeUntilNextDrop(type: SpawnerType): Delay {
        val fromLast = timeFromLast[type] ?: return Delay.fromSeconds(0)
        val diff = type.getDelay(upgradeLevels.getOrDefault(type, 1)).millis - Delay.fromTime(fromLast).millis
        if (diff <= 0) {
            return Delay.fromMillis(0)
        }
        return Delay.fromMillis(diff)
    }
}