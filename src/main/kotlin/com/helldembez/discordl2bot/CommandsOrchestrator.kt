package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.helldembez.discordl2bot.services.ChannelService
import com.helldembez.discordl2bot.services.L2AmerikaService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope

private val log = KotlinLogging.logger {}

class CommandsOrchestrator(scope: CoroutineScope) {
    private val channelService = ChannelService(scope)
    private val l2AmerikaService = L2AmerikaService(scope, channelService)

    fun next() = "_ _\n${l2AmerikaService.bossTimesToString()}"
    fun register(channelId: ChannelId) =
        if (!channelService.exists(channelId)) {
            channelService.addChannel(channelId)
            channelService.scheduleJobsForChannel(channelId, l2AmerikaService.bossesData.values.toSet())
            log.info { "Registered channel $channelId" }
            true
        } else {
            log.info { "Channel $channelId already exists" }
            false
        }

    fun unRegister(channelId: ChannelId) {
        channelService.removeChannel(channelId)
        log.info { "Removed channel $channelId" }
    }


}