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
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.util.getEntity
import me.dkim19375.bedwars.plugin.util.removeAI
import org.bukkit.entity.Villager
import java.util.*

class NPCManager(
    private val plugin: BedwarsPlugin, private val gameData: GameData
) {
    fun disableAI() {
        val villagers = getShopVillagers().toList().plus(getUpgradeVillagers().toList())
        for (villager in villagers) {
            villager.removeAI()
            if (getShopVillagersUUID().contains(villager.uniqueId)) {
                villager.customName = "Shop"
                villager.isCustomNameVisible = true
                continue
            }
            if (getUpgradeVillagersUUID().contains(villager.uniqueId)) {
                villager.customName = "Upgrades"
                villager.isCustomNameVisible = true
                continue
            }
            villager.customName = null
        }
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