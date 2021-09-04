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

import me.dkim19375.bedwars.api.data.BedwarsBedData
import me.dkim19375.bedwars.api.enumclass.Team
import me.dkim19375.bedwars.plugin.util.getBedHead
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.material.Bed

@Suppress("DataClassPrivateConstructor")
data class BedData private constructor(val team: Team, val location: Location, val face: BlockFace) : BedwarsBedData {
    override fun toString(): String {
        return "BedData(team=${team.name}, location=$location, face=${face.name})"
    }

    override fun getTeamType(): Team = team

    override fun getBedLocation(): Location = location

    override fun getBlockFace(): BlockFace = face

    override fun clone(team: Team?, location: Location?, face: BlockFace?): BedwarsBedData = clone(
        team = team ?: this.team,
        location = location ?: this.location,
        face = face ?: this.face
    )

    companion object {
        fun getBedData(team: Team, block: Block): BedData =
            BedData(team, block.getBedHead(), (block.state.data as Bed).facing)
    }
}