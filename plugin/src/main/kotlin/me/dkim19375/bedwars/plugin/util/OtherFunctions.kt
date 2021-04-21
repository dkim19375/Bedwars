package me.dkim19375.bedwars.plugin.util

fun <T> T?.default(default: T): T {
    return this?: default
}