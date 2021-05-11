/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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