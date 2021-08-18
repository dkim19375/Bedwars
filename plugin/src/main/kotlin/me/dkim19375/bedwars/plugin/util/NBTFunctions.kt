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

import de.tr7zw.changeme.nbtapi.NBTCompound
import de.tr7zw.changeme.nbtapi.NBTEntity
import de.tr7zw.changeme.nbtapi.NBTItem
import de.tr7zw.changeme.nbtapi.NBTTileEntity
import de.tr7zw.nbtinjector.NBTInjector
import me.dkim19375.bedwars.plugin.BedwarsPlugin
import me.dkim19375.bedwars.plugin.data.MainShopConfigItem
import org.bukkit.block.BlockState
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

private val plugin: BedwarsPlugin by lazy { JavaPlugin.getPlugin(BedwarsPlugin::class.java) }

private const val HOLOGRAM_KEY = "BedwarsArmorStand"
private const val CONFIG_ITEM_KEY = "BedwarsConfigItem"
private const val NO_AI_KEY = "NoAI"

fun <T : Entity> T.getNBT(): NBTCompound {
    val patched = NBTInjector.patchEntity(this)
    return NBTInjector.getNbtData(patched)
}

fun <T : Entity> T.getVanillaNBT(): NBTCompound = NBTEntity(this)

fun ItemStack.getNBT(): NBTItem = NBTItem(this)

fun <T : BlockState> T.getNBT(): NBTTileEntity = NBTTileEntity(this)

fun <T : Entity> T.addAI() {
    getVanillaNBT().setInteger(NO_AI_KEY, 0)
}

fun <T : Entity> T.removeAI() {
    getVanillaNBT().setInteger(NO_AI_KEY, 1)
}

fun ItemStack.setNBTData(item: MainShopConfigItem?): ItemStack = setNBTData(item?.name)

fun ItemStack.setNBTData(item: String?): ItemStack {
    item ?: return this
    return getNBT().apply { setString("bedwars", item) }.item
}

fun <T : BlockState> T.setNBTData(item: MainShopConfigItem?) = setNBTData(item?.name)

fun <T : BlockState> T.setNBTData(item: String?) {
    item ?: return
    getNBT().apply { setString("bedwars", item) }
    update(false, false)
}

fun NBTCompound.getStringOrNull(key: String): String? = if (keys.contains(key)) getString(key) else null


// custom

fun ItemStack.getConfigItem(): MainShopConfigItem? =
    plugin.configManager.getItemFromName(getNBT().getStringOrNull(CONFIG_ITEM_KEY))

fun <T : BlockState> T.getConfigItem(): MainShopConfigItem? =
    plugin.configManager.getItemFromName(getNBT().getStringOrNull(CONFIG_ITEM_KEY))

fun ArmorStand.isHologram(): Boolean = getNBT().keys.contains(HOLOGRAM_KEY)

fun ArmorStand.setHologramNBT(holo: Boolean) = if (holo) getNBT().setByte(HOLOGRAM_KEY, 0) else Unit