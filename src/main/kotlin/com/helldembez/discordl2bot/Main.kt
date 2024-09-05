package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.helldembez.discordl2bot.services.RoleId
import com.jessecorbett.diskord.bot.bot
import com.jessecorbett.diskord.bot.interaction.interactions
import kotlinx.coroutines.coroutineScope

suspend fun main() = coroutineScope {
    val commandsOrchestrator = CommandsOrchestrator(this)
    bot(BOT_TOKEN) {
        interactions {
            slashCommand("next", "Shows when the next raid boss is spawning.") {
                callback {
                    respond {
                        embeds = commandsOrchestrator.nextBoss().toEmbeds().toList()
                        content = commandsOrchestrator.next()
                    }
                }
            }
            slashCommand("next-all", "Shows when the next raid bosses are spawning.") {
                this.callback {
                    respond {
                        content = commandsOrchestrator.nextAll()
                    }
                }
            }
            slashCommand("subscribe", "Subscribes the channel to receive updates when raid bosses are spawning.") {
                val channelId by channelParameter("channel", "Which channel to subscribe")
                val roleId by roleParameter("announcement-role", "Which role to ping when a rb spawns")
                callback {
                    respond {
                        content = if (!commandsOrchestrator.registerChannel(ChannelId(channelId!!), RoleId(roleId!!))) {
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
                        commandsOrchestrator.unRegisterChannel(ChannelId(channelId!!))
                        content = "Unsubscribed from receiving updates"
                    }
                }
            }
        }
    }
}








