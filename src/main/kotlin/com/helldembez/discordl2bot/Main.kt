package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelService
import com.helldembez.discordl2bot.services.L2AmerikaService
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.interaction.interactions
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
//    val channelService = ChannelService(this)
//    val l2AmerikaService = L2AmerikaService(this, channelService).init()
    bot(BOT_TOKEN) {
        bindEvents()

        interactions {
//            next(l2AmerikaService)
//            nextAll(l2AmerikaService)
//            subscribeRbs(channelService, l2AmerikaService)
//            unsubscribeRbs(channelService)
//            subscribeEvents(channelService)
//            unsubscribeEvents(channelService)
        }
    }
}
