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

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.SpawnerHologram
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.isHologram
import me.dkim19375.bedwars.plugin.util.setHologramNBT
import me.dkim19375.bedwars.plugin.util.update
import me.dkim19375.dkimbukkitcore.function.formatAll
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitTask

class SpawnerHologramManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val holograms = mutableSetOf<SpawnerHologram>()
    private var task: BukkitTask? = null

    fun start() {
        stop()
        val config = plugin.config.getConfigurationSection("holograms.display-spawners")
        val types = config?.getKeys(false)?.filter { config.getBoolean(it) }?.mapNotNull(SpawnerType::fromString)
            ?: listOf(SpawnerType.DIAMOND, SpawnerType.EMERALD)
        for (type in types) {
            val locations = game.data.spawners
                .filter { it.type == type }
                .map(SpawnerData::location)
                .map(Location::update)
            for (location in locations) {
                val getEntity: (String, Double) -> Pair<ArmorStand, Location> = { configKey, default ->
                    val height = (plugin.config.get("holograms.heights.$configKey") as? Number)?.toDouble() ?: default
                    val newLoc = location.clone().add(0.0, height, 0.0)
                    newLoc.world.spawn(newLoc, ArmorStand::class.java).setHologramNBT(true).apply {
                        setBasePlate(false)
                        setGravity(false)
                        isMarker = true
                        isVisible = false
                        isCustomNameVisible = true
                        removeWhenFarAway = false
                        canPickupItems = false
                        customName = "TEST"
                    } to newLoc.clone()
                }
                val timeEntity = getEntity("spawn-time", 2.5)
                val typeEntity = getEntity("spawner-type", 2.8)
                holograms.add(
                    SpawnerHologram(
                        type = type,
                        spawnTimeStand = timeEntity.first.uniqueId,
                        typeArmorStand = typeEntity.first.uniqueId,
                        timePos = timeEntity.second,
                        typePos = typeEntity.second
                    )
                )
            }
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 2L, 5L)
    }

    fun stop() {
        task?.cancel()
        task = null
        removeAll()
    }

    private fun update(check: Boolean = true) {
        if (check) {
            var reset = false
            for (holo in holograms.toSet()) {
                if (holo.getTimeArmorStand() == null || holo.getTypeArmorStand() == null) {
                    reset = true
                }
            }
            if (reset) {
                update()
                return
            }
        }
        for (hologram in holograms.toSet()) {
            val section = plugin.config.getConfigurationSection("holograms.spawner-type-text")
            hologram.getTypeArmorStand()?.customName = (when (hologram.type) {
                SpawnerType.IRON -> (section.getString("iron") ?: "&7&lIron")
                SpawnerType.GOLD -> (section.getString("gold") ?: "&6&lGold")
                SpawnerType.DIAMOND -> (section.getString("diamond") ?: "&b&lDiamond")
                SpawnerType.EMERALD -> (section.getString("emerald") ?: "&a&lEmerald")
            }).formatAll()
            val format = plugin.config.getString("holograms.spawner-time-text") ?: "&eSpawns in &c%time% &eseconds"
            val text = format
                .replace("%time%", game.spawnerManager.getTimeUntilNextDrop(hologram.type).seconds.toString())
                .formatAll()
            hologram.getTimeArmorStand()?.customName = text
        }
    }

    private fun removeAll() {
        for (entity in game.data.world.getEntitiesByClass(ArmorStand::class.java)) {
            if (!entity.isHologram()) {
                continue
            }
            entity.remove()
        }
        holograms.clear()
    }
}