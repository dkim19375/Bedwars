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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface BedwarsGameAPI {
    @API
    @NotNull
    GameState getState();

    @API
    int getCountdown();

    @API
    long getElapsedTime();

    @API
    @NotNull
    Map<Team, Set<Player>> getGamePlayers();

    @API
    @NotNull
    Set<Player> getPlayers();

    @API
    @NotNull
    Set<Player> getEliminated();

    @API
    @NotNull
    Set<Player> getPlayersInLobby();

    @API
    @NotNull
    BedwarsGameData getGameData();

    @API
    @NotNull
    Map<Team, Boolean> getBedStatus();

    @API
    @NotNull
    Map<Player, BedwarsPlayerData> getBeforeData();

    @API
    @NotNull
    Map<Player, Integer> getKills();

    @API
    @NotNull
    Map<Player, Team> getTrackers();

    @API
    @NotNull
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
    boolean isEditing();

    @API
    @NotNull
    Result canStart();

    @API
    @NotNull
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
    Team getTeamOfPlayer(@NotNull Player player);

    @API
    @Nullable
    Team getTeamOfPlayer(@NotNull UUID player);

    @API
    void saveMap();

    @API
    void regenerateMap();

    @API
    void regenerateMap(@NotNull Runnable whenDone);

    @API
    @NotNull
    Set<Player> getPlayersInTeam(@NotNull Team team);
}