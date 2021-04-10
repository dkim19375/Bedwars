package me.dkim19375.bedwars.plugin.util


fun <V> Map<String, V>.getKeyFromStr(key: String): String? {
    for (str in keys) {
        if (key.equals(str, ignoreCase = true)) {
            return str
        }
    }
    return null
}

fun Collection<String>.containsIgnoreCase(value: String): Boolean {
    return any { item -> item == value }
}

fun <T> MutableCollection<MutableSet<T>>.getCombinedValues(): List<T> {
    val newList = mutableListOf<T>()
    forEach { c ->
        c.forEach(newList::add)
    }
    return newList
}

fun <K, V> Map<K, V>.getKeyByValue(value: V): K? {
    for (entry in this.entries) {
        val k = entry.key ?: continue
        val v = entry.value ?: continue
        if (v == value) {
            return k
        }
    }
    return null
}

fun <T> List<T>.convertToString(): List<String> {
    return map { item -> item.toString() }
}

fun <T> T.containsAny(vararg element: List<T>): Boolean {
    this?: return false
    element.forEach { tList ->
        for (t in tList) {
            t?: continue
            if (equals(t)) {
                return true
            }
        }
    }
    return false
}

fun <T> List<T>.containsAny(vararg element: T): Boolean {
    forEach { t ->
        if (element.contains(t)) {
            return true
        }
    }
    return false
}

fun <T> List<T>.combine(other: List<T>): List<T> {
    return plus(other).toList()
}

fun <T> Array<T>.combine(other: Array<T>): Array<T> {
    return plus(other).clone()
}