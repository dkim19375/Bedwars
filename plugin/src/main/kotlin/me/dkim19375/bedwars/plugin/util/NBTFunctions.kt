@file:Suppress("unused")

package me.dkim19375.bedwars.plugin.util

import de.tr7zw.nbtapi.NBTCompound
import de.tr7zw.nbtapi.NBTEntity
import de.tr7zw.nbtapi.NBTItem
import de.tr7zw.nbtinjector.NBTInjector
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

fun <T : Entity> T.getNBT(): NBTCompound {
    NBTInjector.patchEntity(this)
    return NBTInjector.getNbtData(this)
}

fun <T : Entity> T.getVanillaNBT(): NBTCompound = NBTEntity(this)

fun ItemStack.getNBT(): NBTCompound {
    return NBTItem(this)
}

fun <T : Entity> T.removeAI() {
    getVanillaNBT().setInteger("NoAI", 1)
}