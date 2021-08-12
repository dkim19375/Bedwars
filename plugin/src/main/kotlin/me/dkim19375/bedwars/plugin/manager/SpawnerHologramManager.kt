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
                val getEntity: (String, Double) -> ArmorStand = { configKey, default ->
                    val height = (plugin.config.get("holograms.heights.$configKey") as? Number)?.toDouble() ?: default
                    val newLoc = location.add(0.0, height, 0.0)
                    location.world.spawn(newLoc, ArmorStand::class.java).apply {
                        setHologramNBT(true)
                        setBasePlate(false)
                        isMarker = true
                        isVisible = false
                        isCustomNameVisible = true
                        removeWhenFarAway = false
                        canPickupItems = false
                    }
                }
                val timeEntity = getEntity("spawn-time", 2.5)
                val spawnerEntity = getEntity("spawner-type", 2.8)
                holograms.add(SpawnerHologram(type, timeEntity.uniqueId, spawnerEntity.uniqueId))
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
                start()
                update()
                return
            }
        }
        for (hologram in holograms.toSet()) {
            val section = plugin.config.getConfigurationSection("holograms.spawner-type-text")
            hologram.getTypeArmorStand()?.customName = when (hologram.type) {
                SpawnerType.IRON -> (section.getString("iron") ?: "&7&lIron").formatAll()
                SpawnerType.GOLD -> (section.getString("gold") ?: "&6&lGold").formatAll()
                SpawnerType.DIAMOND -> (section.getString("diamond") ?: "&b&lDiamond").formatAll()
                SpawnerType.EMERALD -> (section.getString("emerald") ?: "&a&lEmerald").formatAll()
            }
            val format = plugin.config.getString("holograms.spawner-time-text") ?: "&eSpawns in &c%time% &eseconds"
            val text =
                format.replace("%time%", game.spawnerManager.getTimeUntilNextDrop(hologram.type).seconds.toString())
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