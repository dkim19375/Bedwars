package me.dkim19375.bedwars.plugin.util

class Delay private constructor(time: Long) : Cloneable {
    val millis: Long = time
    val seconds: Long = time / 1000
    val ticks: Long = time / 50

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        fun fromMillis(time: Long) = Delay(time)
        fun fromTicks(time: Long) = Delay(time * 50)
        fun fromSeconds(time: Long) = Delay(time * 1000)
        fun fromTime(before: Long) = fromTime(before, System.currentTimeMillis())
        fun fromTime(before: Long, after: Long) = fromMillis(after - before)
    }

    public override fun clone(): Delay {
        return clone(millis)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun clone(millis: Long = this.millis): Delay {
        return Delay(millis)
    }
}