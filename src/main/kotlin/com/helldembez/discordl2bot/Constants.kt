package com.helldembez.discordl2bot

import arrow.core.getOrElse
import arrow.core.toOption
import com.jessecorbett.diskord.api.channel.Embed
import com.jessecorbett.diskord.api.channel.EmbedImage
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

enum class BOSS_NAMES(private val boss: String, private val imgUrl: String? = null) {
    VALAKAS(
        "Valakas",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982157770362892/Valakas.jpg?ex=66dab7d9&is=66d96659&hm=26a11ae763414ddcddcaff43f3e2a86986e3a43abf9b9a2ac2cd6e96bf9b1b80&"
    ),
    BAIUM(
        "Baium",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982158529531934/Baium.jpg?ex=66dab7da&is=66d9665a&hm=e4105bf42a9cfcaf5296ff6bb11d6bc9a5039ad2058390ea0d43b23b9d8c0b27&"
    ),
    ANTHARAS(
        "Antharas",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982158122422334/Antharas.jpg?ex=66dab7d9&is=66d96659&hm=8ea059867355da0c1f3067ffc6fb16c595e4d4c573387e9c2530617ec17eebf6&"
    ),
    BELETH(
        "Beleth",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982158952894546/Beleth.jpg?ex=66dab7da&is=66d9665a&hm=8348af5317f4b96f5843529c3a3047904193f104cb4b0cfb4f6b082518045de2&"
    ),
    QUEENANT(
        "Queen Ant",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982160148533398/Queen_Ant.jpg?ex=66dab7da&is=66d9665a&hm=8181072b46190f049e9f7f8b352c2fb4fdc3bed5efec6af2463d61b677a2e41a&"
    ),
    MARDIL(
        "Immortal Savior Mardil",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982159552680039/Mardil.png?ex=66dab7da&is=66d9665a&hm=9123be2ebf5e9c01835ddd38335a4652a52101b7d9135c92a64f13eab4ffdd7d&"
    ),
    LINDVIOR(
        "Lindvior",
        "https://cdn.discordapp.com/attachments/1239639690596978788/1280982159229980775/Lindvior.jpg?ex=66dab7da&is=66d9665a&hm=9dd15acf2519c3a09b7f2b3885285b1a6215be326cb97a490532a6fbf5285809&"
    ),
    TERRITORYWAR("Territory War"),
    SIEGE("Siege");

    operator fun invoke(): String {
        return boss
    }

    companion object {
        fun fromBossName(name: String) = entries.first { it.boss == name }
    }

    fun toEmbeds() =
        this.imgUrl.toOption().map { Embed(image = EmbedImage(it, imageHeight = 200, imageWidth = 200)) }
            .map(::arrayOf).getOrElse(::emptyArray)
}

enum class HTML_TAGS(private val tag: String) {
    TABLE("table"),
    TD("td"),
    TR("tr");

    operator fun invoke(): String {
        return tag
    }
}