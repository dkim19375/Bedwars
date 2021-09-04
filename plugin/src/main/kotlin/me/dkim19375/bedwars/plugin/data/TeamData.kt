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

import me.dkim19375.bedwars.api.data.BedwarsTeamData
import me.dkim19375.bedwars.api.enumclass.Team
import org.bukkit.Location

data class TeamData(val team: Team, val location: Location) : BedwarsTeamData {
    override fun toString(): String = "TeamData(team=${team.name}, spawn=$location)"

    override fun getTeamType(): Team = team

    override fun getSpawn(): Location = location

    override fun clone(team: Team?, location: Location?): BedwarsTeamData = copy(
        team = team ?: this.team,
        location = location ?: this.location
    )
}