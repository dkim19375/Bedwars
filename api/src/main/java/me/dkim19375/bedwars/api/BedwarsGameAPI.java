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
import me.dkim19375.bedwars.api.data.BedwarsPlayerData;
import me.dkim19375.bedwars.api.enumclass.GameState;
import me.dkim19375.bedwars.api.enumclass.Result;
import me.dkim19375.bedwars.api.enumclass.Team;
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface BedwarsGameAPI {
    @API
    @NotNull
    @Contract(pure = true)
    GameState getState();

    @API
    @Contract(pure = true)
    int getCountdown();

    @API
    @Contract(pure = true)
    long getElapsedTime();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<Team, Set<Player>> getGamePlayers();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Set<Player> getPlayers();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Set<Player> getEliminated();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Set<Player> getPlayersInLobby();

    @API
    @NotNull
    @Contract(pure = true)
    BedwarsGameData getGameData();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<Team, Boolean> getBedStatus();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<Player, BedwarsPlayerData> getBeforeData();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<Player, Integer> getKills();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Map<Player, Team> getTrackers();

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Set<Player> getSpectators();

    @API
    @NotNull
    Result start();

    @API
    @NotNull
    Result start(boolean force);

    @API
    void stop(@Nullable Player winner, @NotNull Team team);

    @API
    void forceStop();

    @API
    void forceStop(@NotNull Runnable whenDone);

    @API
    @Contract(pure = true)
    boolean isEditing();

    @API
    @NotNull
    @Contract(pure = true)
    Result canStart();

    @API
    @NotNull
    @Contract(pure = true)
    Result canStart(boolean force);

    @API
    void update();

    @API
    void update(boolean force);

    @API
    @NotNull
    Result addPlayer(@NotNull Player player);

    @API
    void broadcast(@NotNull String text);

    @API
    void revertPlayer(@NotNull Player player);

    @API
    void leavePlayer(@NotNull Player player);

    @API
    void leavePlayer(@NotNull Player player, boolean update);

    @API
    @Nullable
    @Contract(pure = true)
    Team getTeamOfPlayer(@NotNull Player player);

    @API
    @Nullable
    @Contract(pure = true)
    Team getTeamOfPlayer(@NotNull UUID player);

    @API
    void saveMap();

    @API
    void regenerateMap();

    @API
    void regenerateMap(@NotNull Runnable whenDone);

    @API
    @NotNull
    @Unmodifiable
    @Contract(pure = true)
    Set<Player> getPlayersInTeam(@NotNull Team team);
}