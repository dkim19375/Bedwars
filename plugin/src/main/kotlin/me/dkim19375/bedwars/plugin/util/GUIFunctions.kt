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

import dev.triumphteam.gui.builder.item.BaseItemBuilder
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.builder.item.SkullBuilder
import org.bukkit.inventory.ItemFlag

fun <B : BaseItemBuilder<out B>> BaseItemBuilder<out B>.name(name: String): B = name(name.toComponent())

fun ItemBuilder.lore(vararg lore: String): ItemBuilder = lore(lore.toList())

fun ItemBuilder.lore(lore: List<String>): ItemBuilder = ItemBuilder.from(build().apply {
    itemMeta = itemMeta?.apply {
        this.lore = (this.lore ?: mutableListOf()).apply { addAll(lore) }
    }
})

fun SkullBuilder.lore(vararg lore: String): SkullBuilder = lore(lore.toList())

fun SkullBuilder.lore(lore: List<String>): SkullBuilder = ItemBuilder.skull(build().apply {
    itemMeta = itemMeta?.apply {
        this.lore = (this.lore ?: mutableListOf()).apply { addAll(lore) }
    }
})

fun <B : BaseItemBuilder<out B>> BaseItemBuilder<out B>.addAllFlags(): B = ItemFlag.values().toList()
    .minus(listOf(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS))
    .map { flags(it) }
    .first()