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

import me.dkim19375.dkimcore.annotation.API;

public interface BedwarsStatisticsData {
    @API
    int getKillCount();

    @API
    void setKillCount(int amount);

    @API
    int getFinalKillCount();

    @API
    void setFinalKillCount(int amount);

    @API
    int getDeathCount();

    @API
    void setDeathCount(int amount);

    @API
    int getFinalDeathCount();

    @API
    void setFinalDeathCount(int amount);

    @API
    int getWinCount();

    @API
    void setWinCount(int amount);

    @API
    int getLossCount();

    @API
    void setLossCount(int amount);

    @API
    int getBedsBrokenCount();

    @API
    void setBedsBrokenCount(int amount);

    @API
    double getKillDeathRatio();

    @API
    double getFinalKillDeathRatio();

    @API
    double getWinLossRatio();
}