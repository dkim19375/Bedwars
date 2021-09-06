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
import org.jetbrains.annotations.Contract;

public interface BedwarsStatisticsData {
    @API
    @Contract(pure = true)
    int getKillCount();

    @API
    void setKillCount(int amount);

    @API
    @Contract(pure = true)
    int getFinalKillCount();

    @API
    void setFinalKillCount(int amount);

    @API
    @Contract(pure = true)
    int getDeathCount();

    @API
    void setDeathCount(int amount);

    @API
    @Contract(pure = true)
    int getFinalDeathCount();

    @API
    void setFinalDeathCount(int amount);

    @API
    @Contract(pure = true)
    int getWinCount();

    @API
    void setWinCount(int amount);

    @API
    @Contract(pure = true)
    int getLossCount();

    @API
    void setLossCount(int amount);

    @API
    @Contract(pure = true)
    int getBedsBrokenCount();

    @API
    void setBedsBrokenCount(int amount);

    @API
    @Contract(pure = true)
    double getKillDeathRatio();

    @API
    @Contract(pure = true)
    double getFinalKillDeathRatio();

    @API
    @Contract(pure = true)
    double getWinLossRatio();
}