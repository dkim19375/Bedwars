package me.dkim19375.bedwars.plugin.util

inline fun <F, reified T> F?.safeCast(to: Class<T>): T? {
    this?: return null
    if (this !is T) {
        return null
    }
    return this
}