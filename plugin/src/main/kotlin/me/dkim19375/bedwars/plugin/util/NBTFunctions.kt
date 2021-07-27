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
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

private val plugin: BedwarsPlugin by lazy { JavaPlugin.getPlugin(BedwarsPlugin::class.java) }

fun <T : Entity> T.getNBT(): NBTCompound {
    NBTInjector.patchEntity(this)
    return NBTInjector.getNbtData(this)
}

fun <T : Entity> T.getVanillaNBT(): NBTCompound = NBTEntity(this)

fun ItemStack.getNBT(): NBTItem = NBTItem(this)

fun <T : BlockState> T.getNBT(): NBTTileEntity = NBTTileEntity(this)

fun <T : Entity> T.addAI() {
    getVanillaNBT().setInteger("NoAI", 0)
}

fun <T : Entity> T.removeAI() {
    getVanillaNBT().setInteger("NoAI", 1)
}

fun ItemStack.setNBTData(item: MainShopConfigItem?): ItemStack = setNBTData(item?.name)

fun ItemStack.setNBTData(item: String?): ItemStack {
    item ?: return this
    return getNBT().apply { setString("bedwars", item) }.item
}

fun ItemStack.getConfigItem(): MainShopConfigItem? = plugin.configManager.getItemFromName(getNBT().getString("bedwars"))

fun <T : BlockState> T.setNBTData(item: MainShopConfigItem?) = setNBTData(item?.name)

fun <T : BlockState> T.setNBTData(item: String?) {
    item ?: return
    getNBT().apply { setString("bedwars", item) }
    update(false, false)
}

fun <T : BlockState> T.getConfigItem(): MainShopConfigItem? = plugin.configManager.getItemFromName(getNBT().getString("bedwars"))