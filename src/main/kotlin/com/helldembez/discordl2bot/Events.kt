package com.helldembez.discordl2bot

import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.EventDispatcherWithContext
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.util.isFromUser
import kotlin.random.Random


fun BotBase.bindEvents() {
    events {
        onMessageUpdate { maxReply(this, it) }
        onMessageCreate { maxReply(this, it) }
    }
}

suspend fun maxReply(context: EventDispatcherWithContext, message: Message) {
    with(context) {
        if (message.usersMentioned.any { it.id == this.botUser.id } && message.isFromUser) {
            message.respond("Hello.")
        } else if (message.isFromUser && message.content.contains("max") && Random.nextInt(0, 10) > 6) {
            message.respond("Hello.")
        } else {
        }
    }

}