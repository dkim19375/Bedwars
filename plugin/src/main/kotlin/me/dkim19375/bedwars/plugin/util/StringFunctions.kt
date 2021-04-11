package me.dkim19375.bedwars.plugin.util

import me.dkim19375.dkim19375core.UUIDUtils
import org.bukkit.ChatColor
import java.util.*

fun String.toUUID(): UUID? {
    return UUIDUtils.getFromString(this)
}

fun String.setGray(): String {
    return "${ChatColor.GRAY}$this"
}