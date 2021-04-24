package me.dkim19375.bedwars.plugin.data

import com.google.common.collect.ImmutableList
import org.apache.commons.lang.Validate
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionBrewer
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionType


/**
 * Represents a minecraft potion
 */
@Suppress("MemberVisibilityCanBePrivate", "Deprecation", "Unused")
class Potion(
    var type: PotionType?
) {
    private var extended = false
    var isSplash = false
    var level = 1

    /**
     *
     * @return the name id
     */
    var nameId = -1
        private set
    /**
     * Returns the [PotionType] of this potion.
     *
     * @return The type of this potion
     */

    /**
     * @param type the type of the potion
     * @param tier the tier of the potion
     */
    @Deprecated("In favour of {@link #Potion(PotionType, int)}")
    constructor(type: PotionType, tier: Tier) : this(type, if (tier == Tier.TWO) 2 else 1) {
        Validate.notNull(type, "Type cannot be null")
    }

    /**
     * @param type the type of the potion
     * @param tier the tier of the potion
     * @param splash whether the potion is a splash potion
     */
    @Deprecated("In favour of {@link #Potion(PotionType, int, boolean)}")
    constructor(type: PotionType, tier: Tier, splash: Boolean) : this(type, if (tier == Tier.TWO) 2 else 1, splash)

    /**
     * @param type the type of the potion
     * @param tier the tier of the potion
     * @param splash whether the potion is a splash potion
     * @param extended whether the potion has an extended duration
     */
    @Deprecated(
        """In favour of {@link #Potion(PotionType, int, boolean,
     *     boolean)}"""
    )
    constructor(type: PotionType, tier: Tier, splash: Boolean, extended: Boolean) : this(type, tier, splash) {
        this.extended = extended
    }

    /**
     * Create a new potion of the given type and level.
     *
     * @param type The type of potion.
     * @param level The potion's level.
     */
    constructor(type: PotionType, level: Int) : this(type) {
        this.level = level
    }

    /**
     * Create a new potion of the given type and level.
     *
     * @param type The type of potion.
     * @param level The potion's level.
     * @param splash Whether it is a splash potion.
     */
    @Deprecated(
        """In favour of using {@link #Potion(PotionType)} with {@link
     *     #splash()}."""
    )
    constructor(type: PotionType, level: Int, splash: Boolean) : this(type, level) {
        isSplash = splash
    }

    /**
     * Create a new potion of the given type and level.
     *
     * @param type The type of potion.
     * @param level The potion's level.
     * @param splash Whether it is a splash potion.
     * @param extended Whether it has an extended duration.
     */
    @Deprecated(
        """In favour of using {@link #Potion(PotionType)} with {@link
     *     #extend()} and possibly {@link #splash()}."""
    )
    constructor(type: PotionType, level: Int, splash: Boolean, extended: Boolean) : this(type, level, splash) {
        this.extended = extended
    }

    /**
     * Create a potion with a specific name.
     *
     * @param name The name index (0-63)
     */
    constructor(name: Int) : this(PotionType.getByDamageValue(name and POTION_BIT)) {
        nameId = name and NAME_BIT
        if (name and POTION_BIT == 0) {
            // If it's 0 it would've become PotionType.WATER, but it should actually be mundane potion
            type = null
        }
    }

    /**
     * Chain this to the constructor to make the potion a splash potion.
     *
     * @return The potion.
     */
    fun splash(): Potion {
        isSplash = true
        return this
    }

    /**
     * Chain this to the constructor to extend the potion's duration.
     *
     * @return The potion.
     */
    fun extend(): Potion {
        setHasExtendedDuration(true)
        return this
    }

    /**
     * Applies the effects of this potion to the given [ItemStack]. The
     * ItemStack must be a potion.
     *
     * @param to The itemstack to apply to
     */
    fun apply(to: ItemStack) {
        Validate.notNull(to, "itemstack cannot be null")
        Validate.isTrue(to.type == Material.POTION, "given itemstack is not a potion")
        to.durability = toDamageValue()
    }

    /**
     * Applies the effects that would be applied by this potion to the given
     * [LivingEntity].
     *
     * @see LivingEntity.addPotionEffects
     * @param to The entity to apply the effects to
     */
    fun apply(to: LivingEntity) {
        Validate.notNull(to, "entity cannot be null")
        to.addPotionEffects(effects)
    }



    /**
     * Returns a collection of [PotionEffect]s that this [Potion]
     * would confer upon a [LivingEntity].
     *
     * @see PotionBrewer.getEffectsFromDamage
     * @see Potion.toDamageValue
     * @return The effects that this potion applies
     */
    val effects: Collection<PotionEffect>
        get() = if (type == null) ImmutableList.of() else brewer!!.getEffectsFromDamage(toDamageValue().toInt())

    @get:Deprecated("")
    var tier: Tier
        get() = if (level == 2) Tier.TWO else Tier.ONE
        set(tier) {
            Validate.notNull(tier, "tier cannot be null")
            level = if (tier == Tier.TWO) 2 else 1
        }

    /**
     * Returns whether this potion has an extended duration.
     *
     * @return Whether this potion has extended duration
     */
    fun hasExtendedDuration(): Boolean {
        return extended
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = prime + level
        result = prime * result + if (extended) 1231 else 1237
        result = prime * result + if (isSplash) 1231 else 1237
        result = prime * result + if (type == null) 0 else type.hashCode()
        return result
    }

    /**
     * Set whether this potion has extended duration. This will cause the
     * potion to have roughly 8/3 more duration than a regular potion.
     *
     * @param isExtended Whether the potion should have extended duration
     */
    fun setHasExtendedDuration(isExtended: Boolean) {
        Validate.isTrue(type == null || !type!!.isInstant, "Instant potions cannot be extended")
        extended = isExtended
    }

    /**
     * Converts this potion to a valid potion damage short, usable for potion
     * item stacks.
     *
     * @return The damage value of this potion
     */
    fun toDamageValue(): Short {
        var damage: Int
        when (type) {
            PotionType.WATER -> {
                return 0
            }
            null -> {
                // Without this, mundanePotion.toDamageValue() would return 0
                damage = (if (nameId == 0) 8192 else nameId)
            }
            else -> {
                damage = (level - 1)
                damage = damage shl TIER_SHIFT
                damage = damage or type!!.damageValue
            }
        }
        if (isSplash) {
            damage = damage or SPLASH_BIT
        }
        if (extended) {
            damage = damage or EXTENDED_BIT
        }
        return damage.toShort()
    }

    /**
     * Converts this potion to an [ItemStack] with the specified amount
     * and a correct damage value.
     *
     * @param amount The amount of the ItemStack
     * @return The created ItemStack
     */
    fun toItemStack(amount: Int): ItemStack {
        return ItemStack(Material.POTION, amount, toDamageValue())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Potion

        if (type != other.type) return false
        if (extended != other.extended) return false
        if (isSplash != other.isSplash) return false
        if (level != other.level) return false

        return true
    }

    enum class Tier(val damageBit: Int) {
        ONE(0), TWO(0x20);

        companion object {
            fun getByDamageBit(damageBit: Int): Tier? {
                for (tier in values()) {
                    if (tier.damageBit == damageBit) {
                        return tier
                    }
                }
                return null
            }
        }
    }

    companion object {
        /**
         * Returns an instance of [PotionBrewer].
         *
         * @return An instance of PotionBrewer
         */
        var brewer: PotionBrewer? = null
            private set
        private const val EXTENDED_BIT = 0x40
        private const val POTION_BIT = 0xF
        private const val SPLASH_BIT = 0x4000
        private const val TIER_BIT = 0x20
        private const val TIER_SHIFT = 5
        private const val NAME_BIT = 0x3F

        /**
         *
         * @param damage the damage value
         * @return the produced potion
         */
        fun fromDamage(damage: Int): Potion {
            val type = PotionType.getByDamageValue(damage and POTION_BIT)
            var potion: Potion
            potion = if (type == null || type == PotionType.WATER) {
                Potion(damage and NAME_BIT)
            } else {
                var level = damage and TIER_BIT shr TIER_SHIFT
                level++
                Potion(type, level)
            }
            if (damage and SPLASH_BIT > 0) {
                potion = potion.splash()
            }
            if ((type == null || !type.isInstant) && damage and EXTENDED_BIT > 0) {
                potion = potion.extend()
            }
            return potion
        }

        fun fromItemStack(item: ItemStack): Potion? {
            if (item.type != Material.POTION) {
                return null
            }
            return fromDamage(item.durability.toInt())
        }

        /**
         * Sets the current instance of [PotionBrewer]. Generally not to be
         * used from within a plugin.
         *
         * @param other The new PotionBrewer
         */
        fun setPotionBrewer(other: PotionBrewer?) {
            require(brewer == null) { "brewer can only be set internally" }
            brewer = other
        }
    }

    init {
        if (type != null) {
            nameId = type!!.damageValue
        }
        if (type == null || type == PotionType.WATER) {
            level = 0
        }
    }
}