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

import me.dkim19375.bedwars.api.data.BedwarsSpawnerData
import me.dkim19375.bedwars.api.enumclass.SpawnerTypes
import me.dkim19375.bedwars.plugin.enumclass.SpawnerType
import org.bukkit.Location

data class SpawnerData(val type: SpawnerType, val location: Location) : BedwarsSpawnerData {

    override fun toString(): String {
        return "SpawnerData(type=${type.name}, location=$location)"
    }

    override fun getType(): SpawnerTypes = type.type

    override fun getSpawnerLocation(): Location = location

    override fun clone(type: SpawnerTypes?, location: Location?): BedwarsSpawnerData = copy(
        type = SpawnerType.fromAPIType(type) ?: this.type,
        location = location ?: this.location
    )
}