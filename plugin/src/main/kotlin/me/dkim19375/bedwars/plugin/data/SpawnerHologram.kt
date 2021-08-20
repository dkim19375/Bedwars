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

package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import me.dkim19375.bedwars.plugin.util.getEntity
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import java.util.*

data class SpawnerHologram(
    val type: SpawnerType,
    val spawnTimeStand: UUID,
    val typeArmorStand: UUID,
    val timePos: Location,
    val typePos: Location
) {
    fun getTimeArmorStand(): ArmorStand? = spawnTimeStand.getEntity() as? ArmorStand
    fun getTypeArmorStand(): ArmorStand? = typeArmorStand.getEntity() as? ArmorStand

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpawnerHologram

        if (type != other.type) return false
        if (typeArmorStand != other.typeArmorStand) return false
        if (spawnTimeStand != other.spawnTimeStand) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + typeArmorStand.hashCode()
        result = 31 * result + spawnTimeStand.hashCode()
        return result
    }
}