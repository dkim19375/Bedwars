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

@file:Suppress("unused")

package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.compat.abstract.NBTUtilitiesAbstract
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack

private lateinit var plugin: BedwarsPlugin
private lateinit var nbtUtils: NBTUtilitiesAbstract

fun initNBTVariables(bedwarsPlugin: BedwarsPlugin) {
    plugin = bedwarsPlugin
    nbtUtils = NBTUtilitiesAbstract.getInstance(plugin)
}

fun <T : LivingEntity> T.addAI() = nbtUtils.addAI(this)

fun <T : LivingEntity> T.removeAI() = nbtUtils.removeAI(this)

fun ItemStack.setConfigItem(item: MainShopConfigItem?): ItemStack = nbtUtils.setConfigItem(this, item?.name)

fun ItemStack.setConfigItem(item: String?): ItemStack = nbtUtils.setConfigItem(this, item)


// custom

fun ItemStack.getConfigItem(): MainShopConfigItem? = plugin.shopConfigManager.getItemFromName(nbtUtils.getConfigItem(this))

fun ArmorStand.isHologram(): Boolean = nbtUtils.isHologram(this)

fun ArmorStand.setHologramNBT(holo: Boolean): ArmorStand = nbtUtils.setHologramNBT(this, holo)

fun ItemStack.setUnbreakable(unbreakable: Boolean): ItemStack = nbtUtils.setUnbreakable(this, unbreakable)

fun Item.setDrop(drop: Boolean): Item = nbtUtils.setDrop(this, drop)

fun Item.isDrop(): Pair<Boolean, Item> = nbtUtils.isDrop(this)

fun <T : Entity> T.disableDrops(): T = nbtUtils.disableDrops(this)

fun <T : Entity> T.isDropsDisabled(): Pair<Boolean, T> = nbtUtils.isDropsDisabled(this)