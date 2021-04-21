package me.dkim19375.bedwars.plugin.manager

import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.util.combine
import me.dkim19375.bedwars.plugin.util.getEntity
import me.dkim19375.bedwars.plugin.util.removeAI
import org.bukkit.entity.Villager
import java.util.*

class NPCManager(
    private val plugin: BedwarsPlugin, private val gameData: GameData
) {
    fun disableAI() {
        val villagers = getShopVillagers().toList().combine(getUpgradeVillagers().toList())
        villagers.forEach(Villager::removeAI)
    }

    fun removeVillager(villager: UUID) {
        if (gameData.shopVillagers.contains(villager)) {
            val villagers = gameData.shopVillagers.toMutableSet()
            villagers.remove(villager)
            gameData.copy(shopVillagers = villagers.toSet()).save(plugin)
        }
        if (gameData.upgradeVillagers.contains(villager)) {
            val villagers = gameData.upgradeVillagers.toMutableSet()
            villagers.remove(villager)
            gameData.copy(upgradeVillagers = villagers.toSet()).save(plugin)
        }
    }

    @Suppress("DuplicatedCode")
    fun getShopVillagers(): Set<Villager> {
        val set = mutableSetOf<Villager>()
        for (uuid in gameData.shopVillagers.toList()) {
            val entity = uuid.getEntity()
            if (entity !is Villager) {
                removeVillager(uuid)
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
            if (entity !is Villager) {
                removeVillager(uuid)
                continue
            }
            set.add(entity)
        }
        return set.toSet()
    }

    fun getShopVillagersUUID() = getShopVillagers().map(Villager::getUniqueId).toSet()

    fun getUpgradeVillagersUUID() = getUpgradeVillagers().map(Villager::getUniqueId).toSet()
}