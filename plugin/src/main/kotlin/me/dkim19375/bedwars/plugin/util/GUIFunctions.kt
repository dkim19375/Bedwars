package me.dkim19375.bedwars.plugin.util

import me.mattstudios.mfgui.gui.components.util.ItemBuilder

fun ItemBuilder.addLore(vararg lore: String): ItemBuilder {
    val lores = build().itemMeta!!.lore
    lores.addAll(lore)
    setLore(*lores.toTypedArray())
    return this
}