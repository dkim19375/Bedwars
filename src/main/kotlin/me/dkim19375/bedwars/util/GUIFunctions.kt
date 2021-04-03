package me.dkim19375.bedwars.util

import me.mattstudios.mfgui.gui.components.util.ItemBuilder

fun ItemBuilder.addLore(vararg lore: String): ItemBuilder {
    //TODO make sure
    val lores = build().itemMeta!!.lore
    lores.addAll(lore)
    setLore(*lores.toTypedArray())
    return this
}