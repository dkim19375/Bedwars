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

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor

fun String.setGray(): String {
    return "${ChatColor.GRAY}$this"
}

fun String.capAndFormat(): String = StringUtils.capitalize(this.lowercase().replace("_", " "))

fun String.toComponent(): TextComponent = LegacyComponentSerializer.legacySection().deserialize(this)

fun List<String>.toComponents(): List<TextComponent> = map(String::toComponent)

fun Array<String>.toComponents(): Array<TextComponent> = map(String::toComponent).toTypedArray()