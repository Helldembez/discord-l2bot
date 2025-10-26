package com.helldembez.discordl2bot

import arrow.core.firstOrNone
import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.EventDispatcherWithContext
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.isFromUser
import kotlin.random.Random


fun BotBase.bindEvents() {
    events {
        onInit {
            it.guilds.forEach { guild ->
                println(guild.id)
                guildIds.add(guild.id)
            }
        }
        onMessageUpdate { maxReply(this, it) }
        onMessageCreate { maxReply(this, it) }
    }
}

suspend fun maxReply(context: EventDispatcherWithContext, message: Message) {
    with(context) {
        if (message.isFromUser && message.author.displayName == "Helldembez") {
            if (message.content.contains("guildinfo")) {
                val text = guildIds.map {
                    "$it:${guild(it).getGuild().name}\n"
                }.joinToString()
                message.respond(text)
            }
            if (message.content.contains("leaveguild")) {
                guildIds.firstOrNone{
                    message.content.contains(it)
                }.onSome {
                    val guild = guild(it)
                    message.respond("leaving guild: '${guild.getGuild().name}'.")
                    guild.leave()
                }
            }
        }
        if (message.usersMentioned.any { it.id == this.botUser.id } && message.isFromUser) {
            message.respond("Hello.")
        } else if (message.isFromUser && message.content.contains("max") && Random.nextInt(0, 10) > 6) {
            message.respond("Hello.")
        } else {
        }
    }

}