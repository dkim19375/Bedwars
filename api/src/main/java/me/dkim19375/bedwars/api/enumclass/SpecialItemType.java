package me.dkim19375.bedwars.api.enumclass;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public enum SpecialItemType {
    BED_BUGS,
    DREAM_DEFENDER,
    MAGIC_MILK,
    BRIDGE_EGGS;

    @Nullable
    @Contract(value = "null -> null", pure = true)
    public static SpecialItemType fromString(@Nullable String str) {
        if (str == null) {
            return null;
        }
        try {
            return valueOf(str.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}