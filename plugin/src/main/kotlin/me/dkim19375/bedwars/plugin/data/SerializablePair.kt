package me.dkim19375.bedwars.plugin.data

import me.dkim19375.bedwars.plugin.util.serializedPairMapOf
import org.bukkit.configuration.serialization.ConfigurationSerializable

data class SerializablePair<out A, out B>(
    val first: A,
    val second: B
) : ConfigurationSerializable {

    override fun serialize(): Map<String, Any> {
        return serializedPairMapOf(
            ("first" to first!!),
            ("second" to second!!)
        )
    }

    fun toPair(): Pair<A, B> = Pair(first, second)

    /**
     * Returns string representation of the [SerializablePair] including its [first] and [second] values.
     */
    override fun toString(): String = "($first, $second)"
}

/**
 * Creates a tuple of type [SerializablePair] from this and [that].
 *
 * This can be useful for creating [Map] literals with less noise, for example:
 */
infix fun <A, B> A.to(that: B): SerializablePair<A, B> = SerializablePair(this, that)

/**
 * Converts this pair into a list.
 */
fun <T> SerializablePair<T, T>.toList(): List<T> = listOf(first, second)