package me.dkim19375.bedwars.plugin.config

import me.mattstudios.config.SettingsHolder
import me.mattstudios.config.annotations.Comment
import me.mattstudios.config.annotations.Path
import me.mattstudios.config.configurationdata.CommentsConfiguration
import me.mattstudios.config.properties.Property

object MainConfigSettings : SettingsHolder {
    // holograms
    @Path("holograms.display-spawners.iron")
    val IRON_HOLOGRAMS: Property<Boolean> = Property.create(false)

    @Path("holograms.display-spawners.gold")
    val GOLD_HOLOGRAMS: Property<Boolean> = Property.create(false)

    @Path("holograms.display-spawners.diamond")
    val DIAMOND_HOLOGRAMS: Property<Boolean> = Property.create(true)

    @Path("holograms.display-spawners.emerald")
    val EMERALD_HOLOGRAMS: Property<Boolean> = Property.create(true)

    @Path("holograms.heights.spawn-time")
    val HEIGHTS_SPAWN_TIME: Property<Double> = Property.create(3.5)

    @Path("holograms.heights.spawner-type")
    val HEIGHTS_SPAWNER_TYPE: Property<Double> = Property.create(3.8)

    @Path("holograms.heights.tier-type")
    val HEIGHTS_TIER_TYPE: Property<Double> = Property.create(4.1)

    @Path("holograms.spawner-type-text.iron")
    val IRON_HOLOGRAMS_TEXT: Property<String> = Property.create("&7&lIron")

    @Path("holograms.spawner-type-text.gold")
    val GOLD_HOLOGRAMS_TEXT: Property<String> = Property.create("&6&lGold")

    @Path("holograms.spawner-type-text.diamond")
    val DIAMOND_HOLOGRAMS_TEXT: Property<String> = Property.create("&b&lDiamond")

    @Path("holograms.spawner-type-text.emerald")
    val EMERALD_HOLOGRAMS_TEXT: Property<String> = Property.create("&a&lEmerald")

    @Path("holograms.spawner-time-text")
    val SPAWNER_TIME_TEXT: Property<String> = Property.create("&eSpawns in &c%time% &eseconds")

    @Path("holograms.spawner-tier-text")
    val SPAWNER_TIER_TEXT: Property<String> = Property.create("&eTier &c%tier%")

    // parties
    @Path("parties.teleport-automatically")
    @Comment("Make sure that teleports is enabled in the parties.yml in the Parties plugin!")
    val PARTIES_TELEPORT_AUTO: Property<Boolean> = Property.create(true)

    // generator
    @Path("generator.split.enabled")
    val SPLIT_ENABLED: Property<Boolean> = Property.create(true)

    @Path("generator.split.generators")
    val SPLIT_GENERATORS: Property<List<String>> = Property.create(listOf("IRON", "GOLD"))

    // tab
    @Path("tab.name")
    @Comment("Can also use %team_name% for \"Red\" or %team_uppercase% for \"RED\"")
    val TAB_NAME: Property<String> = Property.create("%team_color%&l%team_first_letter% %team_color%%player_name%")

    @Path("tab.hide-players.same-world")
    @Comment("Allows you to see players in the same world even if they're not in the same game")
    val HIDE_SAME_WORLD: Property<Boolean> = Property.create(true)

    @Path("tab.hide-players.enabled")
    val HIDE_ENABLED: Property<Boolean> = Property.create(true)

    // trap
    @Comment("7 blocks from the bed")
    @Path("trap.range")
    val TRAP_RANGE: Property<Int> = Property.create(7)

    @Comment("20 seconds until you can trigger the trap again")
    @Path("trap.cooldown")
    val TRAP_COOLDOWN: Property<Int> = Property.create(20)

    override fun registerComments(conf: CommentsConfiguration) {
        conf.setComment(
            "tab.hide-players",
            "Players in-game won't see players not in the game and players not in the game won't see players in-game",
            "unless in the same world (and same-world is true)"
        )
    }
}