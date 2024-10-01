package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.helldembez.discordl2bot.services.RoleId
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.bot.interaction.interactions
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
    val commandsOrchestrator = CommandsOrchestrator(this)
    bot(BOT_TOKEN) {
        events {
            onMessageUpdate { commandsOrchestrator.maxReply(this, it) }
            onMessageCreate { commandsOrchestrator.maxReply(this, it) }
        }
        interactions {
            slashCommand("next", "Shows when the next raid boss is spawning.") {
                commandsOrchestrator.next(this)
            }
            slashCommand("next-all", "Shows when the next raid bosses are spawning.") {
                commandsOrchestrator.nextAll(this)
            }
            slashCommand("subscribeRbs", "Subscribes the channel to receive updates when raid bosses are spawning.") {
                commandsOrchestrator.registerChannel(this)
            }
            slashCommand(
                "unsubscribeRbs",
                "Unsubscribes the channel from receiving updates when raid bosses are spawning."
            ) {
                commandsOrchestrator.unRegisterChannel(this)
            }
//            slashCommand(
//                "subscribeEvents",
//                "Subscribes the channel to receive updates which and when events are starting."
//            ) {
//                commandsOrchestrator.subscribeEvents(this)
//            }
//            slashCommand(
//                "unsubscribeEvents",
//                "Unsubscribes the channel from receiving updates which and when events are starting."
//            ) {
//                val channelId by channelParameter("channel", "Which channel to unsubscribe")
//                callback {
//                    respond {
//                        commandsOrchestrator.unRegisterChannel(ChannelId(channelId!!))
//                        content = "Unsubscribed from receiving updates"
//                    }
//                }
//            }
        }
    }
}
