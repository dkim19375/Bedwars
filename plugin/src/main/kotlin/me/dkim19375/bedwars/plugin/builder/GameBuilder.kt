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

package me.dkim19375.bedwars.plugin.builder

import me.dkim19375.bedwars.plugin.data.BedData
import me.dkim19375.bedwars.plugin.data.GameData
import me.dkim19375.bedwars.plugin.data.SpawnerData
import me.dkim19375.bedwars.plugin.data.TeamData
import me.dkim19375.bedwars.plugin.enumclass.BuildError
import org.bukkit.Location
import org.bukkit.World
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class GameBuilder(
    var world: World,
    var minPlayers: Int = 2,
    var maxPlayers: Int = 8,
    var teams: MutableSet<TeamData> = mutableSetOf(),
    val shopVillagers: MutableSet<UUID> = mutableSetOf(),
    val upgradeVillagers: MutableSet<UUID> = mutableSetOf(),
    var spawners: MutableSet<SpawnerData> = mutableSetOf(),
    var beds: MutableSet<BedData> = mutableSetOf(),
    var spec: Location? = null,
    var lobby: Location? = null
) {

    fun canBuild(): Set<BuildError> {
        val errors = mutableSetOf<BuildError>()
        spec ?: errors.add(BuildError.SPEC)
        lobby ?: errors.add(BuildError.LOBBY)
        if (teams.isEmpty()) {
            errors.add(BuildError.TEAMS)
        }
        if (shopVillagers.isEmpty()) {
            errors.add(BuildError.SHOP)
        }
        if (upgradeVillagers.isEmpty()) {
            errors.add(BuildError.UPGRADES)
        }
        if (spawners.isEmpty()) {
            errors.add(BuildError.SPAWNERS)
        }
        if (beds.size < teams.size) {
            errors.add(BuildError.NOT_ENOUGH_BEDS)
        }
        return errors.toSet()
    }

    fun build(): GameData? {
        val spec = spec ?: return null
        val lobby = lobby ?: return null
        if (canBuild().isNotEmpty()) {
            return null
        }
        return GameData(
            world,
            minPlayers,
            maxPlayers,
            teams,
            shopVillagers,
            upgradeVillagers,
            spawners,
            beds,
            spec,
            lobby
        )
    }
}