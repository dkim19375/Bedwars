package me.dkim19375.bedwars.plugin.util

import java.util.*

fun String.toUUID(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (_: IllegalArgumentException) {
        null
    }
}