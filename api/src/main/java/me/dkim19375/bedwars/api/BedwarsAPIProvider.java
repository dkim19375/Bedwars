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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BedwarsAPIProvider {
    @Nullable
    private static BedwarsAPI api = null;

    @NotNull
    public static BedwarsAPI getAPI() {
        if (api == null) {
            throw new IllegalStateException("Bedwars API isn't set! Make sure you are calling this in "
                    + "onLoad or onEnable (or after) and have softdepend/depend: Bedwars in plugin.yml");
        }
        return api;
    }

    @ApiStatus.Internal
    public static void register(@NotNull BedwarsAPI api) {
        if (BedwarsAPIProvider.api != null) {
            throw new UnsupportedOperationException("The Bedwars API is already set!");
        }
        BedwarsAPIProvider.api = api;
    }
}