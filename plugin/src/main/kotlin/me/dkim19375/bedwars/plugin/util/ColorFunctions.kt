package me.dkim19375.bedwars.plugin.util

import org.bukkit.ChatColor

fun Boolean.getGreenOrRed() = if (this)
    ChatColor.GREEN else ChatColor.RED

fun Boolean.getGreenOrGray() = if (this)
    ChatColor.GREEN else ChatColor.GRAY