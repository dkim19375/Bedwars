package me.dkim19375.bedwars.util


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
        val k = entry.key?: continue
        val v = entry.value?: continue
        if (v == value) {
            return k
        }
    }
    return null
}