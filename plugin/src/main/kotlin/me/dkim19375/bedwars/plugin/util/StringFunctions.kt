package me.dkim19375.bedwars.plugin.util

import me.dkim19375.dkim19375core.UUIDUtils
import java.util.*

fun String.toUUID(): UUID? {
    return UUIDUtils.getFromString(this)
}