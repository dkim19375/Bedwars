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
import me.dkim19375.bedwars.plugin.config.MainConfigSettings
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.SpawnerHologram
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.isHologram
import me.dkim19375.bedwars.plugin.util.setHologramNBT
import me.dkim19375.bedwars.plugin.util.toRomanNumeral
import me.dkim19375.bedwars.plugin.util.update
import me.dkim19375.dkimbukkitcore.function.formatAll
import me.mattstudios.config.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.scheduler.BukkitTask

class SpawnerHologramManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val holograms = mutableSetOf<SpawnerHologram>()
    private var task: BukkitTask? = null

    fun start() {
        stop()
        val configManager = plugin.mainConfigManager
        val settings = MainConfigSettings
        val types = setOf(
            SpawnerType.IRON to configManager.get(settings.IRON_HOLOGRAMS),
            SpawnerType.GOLD to configManager.get(settings.GOLD_HOLOGRAMS),
            SpawnerType.DIAMOND to configManager.get(settings.DIAMOND_HOLOGRAMS),
            SpawnerType.EMERALD to configManager.get(settings.EMERALD_HOLOGRAMS)
        ).filter(Pair<*, Boolean>::second).map(Pair<SpawnerType, *>::first)
        for (type in types) {
            val locations = game.data.spawners
                .filter { it.type == type }
                .map(SpawnerData::location)
                .map(Location::update)
            for (location in locations) {
                val getEntity: (Property<Double>) -> Pair<ArmorStand, Location> = { setting ->
                    val height = configManager.get(setting)
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
                val timeEntity = getEntity(settings.HEIGHTS_SPAWN_TIME)
                val typeEntity = getEntity(settings.HEIGHTS_SPAWNER_TYPE)
                val tierEntity = getEntity(settings.HEIGHTS_TIER_TYPE)
                holograms.add(
                    SpawnerHologram(
                        type = type,
                        spawnTimeStand = timeEntity.first.uniqueId,
                        typeArmorStand = typeEntity.first.uniqueId,
                        tierArmorStand = tierEntity.first.uniqueId,
                        timePos = timeEntity.second,
                        typePos = typeEntity.second,
                        tierPos = tierEntity.second
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
            if (holograms.any { holo ->
                    holo.getTimeArmorStand() == null || holo.getTypeArmorStand() == null || holo.getTierArmorStand() == null
                }
            ) {
                update()
                return
            }
        }
        val configManager = plugin.mainConfigManager
        for (hologram in holograms.toSet()) {
            hologram.getTypeArmorStand()?.customName = (when (hologram.type) {
                SpawnerType.IRON -> configManager.get(MainConfigSettings.IRON_HOLOGRAMS_TEXT)
                SpawnerType.GOLD -> configManager.get(MainConfigSettings.GOLD_HOLOGRAMS_TEXT)
                SpawnerType.DIAMOND -> configManager.get(MainConfigSettings.DIAMOND_HOLOGRAMS_TEXT)
                SpawnerType.EMERALD -> configManager.get(MainConfigSettings.EMERALD_HOLOGRAMS_TEXT)
            }).formatAll()
            val timeFormat = configManager.get(MainConfigSettings.SPAWNER_TIME_TEXT)
            val timeText = timeFormat
                .replace("%time%", game.spawnerManager.getTimeUntilNextDrop(hologram.type).seconds.toString())
                .formatAll()
            hologram.getTimeArmorStand()?.customName = timeText
            val tierFormat = configManager.get(MainConfigSettings.SPAWNER_TIER_TEXT)
            val tierText = tierFormat
                .replace("%tier%", game.spawnerManager.upgradeLevels.getOrDefault(hologram.type, 1).toRomanNumeral())
                .formatAll()
            hologram.getTierArmorStand()?.customName = tierText
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