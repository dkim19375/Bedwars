package me.dkim19375.bedwars.plugin.util

fun Int.toRomanNumeral() = when (this) {
    0 -> "0"
    1 -> "I"
    2 -> "II"
    3 -> "III"
    4 -> "IV"
    5 -> "V"
    else -> Int.toString()
}

fun Int?.zeroNonNull(): Int {
    return this?: 0
}

fun Int.limit(limit: Int): Int {
    if (this > limit) {
        return limit
    }
    return this
}