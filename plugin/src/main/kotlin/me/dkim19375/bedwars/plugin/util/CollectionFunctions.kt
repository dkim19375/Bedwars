/*
 *     Bedwars, a minigame for spigot
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.bedwars.plugin.util

import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.Team


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