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

import dev.triumphteam.gui.builder.item.ItemBuilder

fun ItemBuilder.name(name: String): ItemBuilder = name(name.toComponent())

fun ItemBuilder.lore(vararg lore: String): ItemBuilder = lore(lore.toList())

fun ItemBuilder.lore(lore: List<String>): ItemBuilder = ItemBuilder.from(build().apply {
    itemMeta = itemMeta?.apply {
        this.lore = (this.lore ?: mutableListOf()).apply { addAll(lore) }
    }
})