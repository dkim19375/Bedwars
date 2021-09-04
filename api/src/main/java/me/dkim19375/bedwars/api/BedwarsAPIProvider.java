package me.dkim19375.bedwars.api;

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

    public static void register(@NotNull BedwarsAPI api) {
        if (BedwarsAPIProvider.api != null) {
            throw new UnsupportedOperationException("The Bedwars API is already set!");
        }
        BedwarsAPIProvider.api = api;
    }
}