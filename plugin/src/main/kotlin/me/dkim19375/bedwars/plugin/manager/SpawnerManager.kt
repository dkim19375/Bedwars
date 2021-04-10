package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.enumclass.GameState
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import org.bukkit.scheduler.BukkitRunnable

class SpawnerManager(private val plugin: BedwarsPlugin, private val game: BedwarsGame) {
    private val timeFromLast = mutableMapOf<SpawnerType, Long>()
    private val upgradeLevels = mutableMapOf<SpawnerType, Int>()

    fun start() {
        val runnable = object : BukkitRunnable() {
            override fun run() {
                if (game.state != GameState.STARTED) {
                    return
                }

            }
        }
    }

    fun getTime
}