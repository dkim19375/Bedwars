package me.dkim19375.bedwars.plugin.enumclass

enum class Permission(val permission: String) {
    START("bedwars.start"),
    HELP("bedwars.help"),
    LIST("bedwars.list"),
    RELOAD("bedwars.reload"),
    JOIN("bedwars.join"),
    LEAVE("bedwars.leave"),
    SETUP("bedwars.setup"),
    STOP("bedwars.stop")
}