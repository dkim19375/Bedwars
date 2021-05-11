/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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