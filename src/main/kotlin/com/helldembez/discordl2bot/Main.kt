package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.interaction.interactions
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
    val commandsOrchestrator = CommandsOrchestrator(this)
    bot(BOT_TOKEN) {
        interactions {
            slashCommand("next", "Shows when the next raid bosses are spawning.") {
                callback {
                    respond {
                        content = commandsOrchestrator.next()
                    }
                }
            }
            slashCommand("subscribe", "Subscribes the channel to receive updates when raid bosses are spawning.") {
                val channelId by channelParameter("channel", "Which channel to subscribe")
                callback {
                    respond {
                        content = if (!commandsOrchestrator.register(ChannelId(channelId!!))) {
                            "Channel already subscribed"
                        } else {
                            "Channel successfully subscribed"
                        }
                    }
                }
            }
            slashCommand(
                "unsubscribe",
                "Unsubscribes the channel from receiving updates when raid bosses are spawning."
            ) {
                val channelId by channelParameter("channel", "Which channel to unsubscribe")
                callback {
                    respond {
                        commandsOrchestrator.unRegister(ChannelId(channelId!!))
                        content = "Unsubscribed from receiving updates"
                    }
                }
            }
        }
    }
}








