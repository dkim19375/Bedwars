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

package me.dkim19375.bedwars.api.data;

import me.dkim19375.bedwars.api.enumclass.Team;
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BedwarsBedData {
    @API
    @NotNull
    Team getTeamType();

    @API
    @NotNull
    Location getBedLocation();

    @API
    @NotNull
    BlockFace getBlockFace();

    @API
    @NotNull
    BedwarsBedData clone(@Nullable Team team, @Nullable Location location, @Nullable BlockFace face);
}
