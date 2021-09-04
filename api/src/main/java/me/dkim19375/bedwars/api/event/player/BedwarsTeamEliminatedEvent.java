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

package me.dkim19375.bedwars.api.event.player;

import me.dkim19375.bedwars.api.enumclass.Team;
import me.dkim19375.dkimcore.annotation.API;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BedwarsTeamEliminatedEvent extends BedwarsPlayerEvent {
    @NotNull
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    private final Team team;
    @NotNull
    private final BedwarsPlayerEliminatedEvent eliminatedEvent;

    public BedwarsTeamEliminatedEvent(@NotNull BedwarsPlayerEliminatedEvent playerEvent, @NotNull Team team) {
        super(playerEvent.getPlayer(), playerEvent.getGame());
        this.team = team;
        eliminatedEvent = playerEvent;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    @API
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    public Team getTeam() {
        return team;
    }

    @NotNull
    public BedwarsPlayerEliminatedEvent getEliminatedEvent() {
        return eliminatedEvent;
    }
}
