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

class Delay private constructor(time: Long) : Cloneable {
    val millis: Long = time
    val seconds: Long = time / 1000
    @Suppress("unused")
    val ticks: Long = time / 50

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        fun fromMillis(time: Long) = Delay(time)
        fun fromTicks(time: Long) = Delay(time * 50)
        fun fromSeconds(time: Long) = Delay(time * 1000)
        fun fromMinutes(time: Long) = Delay(time * 60000)
        fun fromTime(before: Long) = fromTime(before, System.currentTimeMillis())
        fun fromTime(before: Long, after: Long) = fromMillis(after - before)
    }

    public override fun clone(): Delay {
        return clone(millis)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun clone(millis: Long = this.millis): Delay {
        return Delay(millis)
    }

    operator fun minus(other: Delay): Delay = Delay(millis - other.millis)

    operator fun minus(other: Long): Delay = Delay(millis - other)

    operator fun plus(other: Delay): Delay = Delay(millis + other.millis)

    operator fun plus(other: Long): Delay = Delay(millis + other)

    operator fun div(other: Delay): Delay = Delay(millis / other.millis)

    operator fun div(other: Long): Delay = Delay(millis / other)

    operator fun times(other: Delay): Delay = Delay(millis / other.millis)

    operator fun times(other: Long): Delay = Delay(millis / other)

    operator fun compareTo(other: Delay): Int = millis.compareTo(other.millis)

    operator fun compareTo(other: Long): Int = millis.compareTo(other)

    fun formatTime(): String = seconds.formatTime()
}