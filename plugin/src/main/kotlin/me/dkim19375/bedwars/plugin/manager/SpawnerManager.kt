package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.Delay
import me.dkim19375.bedwars.plugin.util.dropItemStraight
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

class SpawnerManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val timeFromLast = mutableMapOf<SpawnerType, Long>()
    private val upgradeLevels = mutableMapOf<SpawnerType, Int>()

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

                for (type in SpawnerType.values()) {
                    if (getTimeUntilNextDrop(type).millis <= 0) {
                        types.add(type)
                        timeFromLast[type] = System.currentTimeMillis()
                    }
                }

                val spawners = game.data.spawners
                for (spawner in spawners) {
                    if (!types.contains(spawner.type)) {
                        continue
                    }
                    val loc = spawner.location
                    val item = loc.world.dropItemStraight(loc, ItemStack(spawner.type.material))
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