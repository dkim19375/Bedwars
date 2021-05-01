package me.dkim19375.bedwars.plugin.util

import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor

fun String.setGray(): String {
    return "${ChatColor.GRAY}$this"
}

fun String.capAndFormat(): String = StringUtils.capitalize(this.toLowerCase().replace("_", " "))