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

package me.dkim19375.bedwars.api;

import me.dkim19375.bedwars.api.data.BedwarsGameData;
import me.dkim19375.bedwars.api.data.BedwarsStatisticsData;
import me.dkim19375.bedwars.api.enumclass.Team;
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.UUID;

public interface BedwarsAPI {
    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<String, BedwarsGameAPI> getGames();

    @API
    @Nullable
    @Contract(value = "null -> null", pure = true)
    BedwarsGameAPI getGame(@Nullable String name);

    @API
    void deleteGame(@NotNull BedwarsGameAPI game);

    @API
    void saveGameData(@NotNull BedwarsGameData data);

    @API
    @Contract(pure = true)
    ItemStack getColored(@NotNull Team team, @NotNull ItemStack item);

    @API
    @NotNull
    BedwarsStatisticsData getStatistics(@NotNull Player player);

    @API
    @NotNull
    BedwarsStatisticsData getStatistics(@NotNull UUID player);
}