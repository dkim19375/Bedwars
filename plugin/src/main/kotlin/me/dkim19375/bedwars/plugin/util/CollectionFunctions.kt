package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.Team
import java.util.*


fun <V> Map<String, V>.getKeyFromStr(key: String): String? {
    for (str in keys) {
        if (key.equals(str, ignoreCase = true)) {
            return str
        }
    }
    return null
}

fun <V> Map<String, V>.getIgnoreCase(other: String): V? {
    for (entry in entries) {
        if (entry.key.equals(other, ignoreCase = true)) {
            return entry.value
        }
    }
    return null
}

fun Set<TeamData>.containsTeam(team: Team): Boolean = getTeam(team) != null

fun Set<TeamData>.getTeam(team: Team): TeamData? {
    return firstOrNull { d -> d.team == team }
}

fun MutableSet<TeamData>.removeTeam(team: Team) {
    removeIf { d -> d.team == team }
}

fun MutableSet<TeamData>.setData(data: TeamData) {
    removeTeam(data.team)
    add(data)
}

fun Collection<String>.containsIgnoreCase(value: String): Boolean {
    return any { item -> item.equals(value, ignoreCase = true) }
}

fun <T> MutableCollection<MutableSet<T>>.getCombinedValues(): List<T> {
    val newList = mutableListOf<T>()
    forEach { c ->
        c.forEach(newList::add)
    }
    return newList
}

fun <T> T.containsAny(vararg element: List<T>): Boolean {
    this ?: return false
    element.forEach { tList ->
        for (t in tList) {
            t ?: continue
            if (equals(t)) {
                return true
            }
        }
    }
    return false
}

fun <T> List<T>.containsAny(vararg element: T): Boolean {
    forEach { t ->
        if (element.contains(t)) {
            return true
        }
    }
    return false
}