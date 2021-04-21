package me.dkim19375.bedwars.plugin.enumclass

enum class BuildError(val message: String) {
    TEAMS("There are no teams!"),
    SHOP("There are no shop villagers!"),
    UPGRADES("There are no upgrade villagers!"),
    SPAWNERS("There are no spawners!"),
    SPEC("There is no spectator location!"),
    LOBBY("There is no lobby location!"),

    NOT_ENOUGH_BEDS("There are not enough beds for each team!")
}