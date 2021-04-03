package me.dkim19375.bedwars.util

enum class GameState(val running: Boolean) {
    STOPPED(false),
    LOBBY(false),
    STARTING(true),
    STARTED(true),
    REGENERATING_WORLD(true)
}