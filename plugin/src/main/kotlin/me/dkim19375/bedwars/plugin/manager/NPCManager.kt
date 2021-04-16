package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.util.combine
import me.dkim19375.bedwars.plugin.util.getEntity
import me.dkim19375.bedwars.plugin.util.removeAI
import org.bukkit.entity.Villager

class NPCManager(
    private val plugin: BedwarsPlugin, private val gameData: GameData
) {
    fun disableAI() {
        val villagers = getShopVillagers().toList().combine(getUpgradeVillagers().toList())
        villagers.forEach(Villager::removeAI)
    }

    @Suppress("DuplicatedCode")
    fun getShopVillagers(): Set<Villager> {
        val set = mutableSetOf<Villager>()
        for (uuid in gameData.shopVillagers.toList()) {
            val entity = uuid.getEntity()
            if (entity == null) {
                val villagers = gameData.shopVillagers.toMutableList()
                villagers.remove(uuid)
                gameData.copy(shopVillagers = villagers).save(plugin)
                continue
            }
            if (entity !is Villager) {
                val villagers = gameData.shopVillagers.toMutableList()
                villagers.remove(uuid)
                gameData.copy(shopVillagers = villagers).save(plugin)
                continue
            }
            set.add(entity)
        }
        return set.toSet()
    }

    @Suppress("DuplicatedCode")
    fun getUpgradeVillagers(): Set<Villager> {
        val set = mutableSetOf<Villager>()
        for (uuid in gameData.upgradeVillagers.toList()) {
            val entity = uuid.getEntity()
            if (entity == null) {
                val villagers = gameData.upgradeVillagers.toMutableList()
                villagers.remove(uuid)
                gameData.copy(upgradeVillagers = villagers).save(plugin)
                continue
            }
            if (entity !is Villager) {
                val villagers = gameData.upgradeVillagers.toMutableList()
                villagers.remove(uuid)
                gameData.copy(upgradeVillagers = villagers).save(plugin)
                continue
            }
            set.add(entity)
        }
        return set.toSet()
    }
}