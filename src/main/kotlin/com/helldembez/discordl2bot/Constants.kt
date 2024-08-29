package com.helldembez.discordl2bot

import java.time.Clock
import java.time.ZoneId

val BOT_TOKEN = try {
    System.getenv("BOT_TOKEN")
} catch (error: Exception) {
    throw RuntimeException(
        "Failed to load bot token. Make sure to define an environment variable called 'BOT_TOKEN'", error
    )
}
val ZONE = ZoneId.of("Europe/Amsterdam")
val CLOCK = Clock.system(ZONE)
enum class BOSS_NAMES(private val boss: String) {
    VALAKAS("Valakas"),
    BAIUM("Baium"),
    ANTHARAS("Antharas"),
    BELETH("Beleth"),
    QUEENANT("Queen Ant"),
    MARDIL("Immortal Savior Mardil"),
    LINDVIOR("Lindvior"),
    TERRITORYWAR("Territory War"),
    SIEGE("Siege");

    operator fun invoke(): String {
        return boss
    }
}



enum class HTML_TAGS(private val tag: String) {
    TABLE("table"),
    TD("td"),
    TR("tr");

    operator fun invoke(): String {
        return tag
    }
}