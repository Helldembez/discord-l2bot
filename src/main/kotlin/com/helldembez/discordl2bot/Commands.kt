package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.helldembez.discordl2bot.services.ChannelService
import com.helldembez.discordl2bot.services.L2AmerikaService
import com.helldembez.discordl2bot.services.RoleId
import com.jessecorbett.diskord.bot.interaction.InteractionBuilder

fun InteractionBuilder.next(l2AmerikaService: L2AmerikaService) {
    slashCommand("next", "Shows when the next raid boss is spawning.") {
        callback {
            respond {
                embeds = l2AmerikaService.bossesData.values.toSet().sort().first().name.toEmbeds().toList()
                content = "_ _\n${l2AmerikaService.bossTimeToString()}"
            }
        }
    }
}

fun InteractionBuilder.nextAll(l2AmerikaService: L2AmerikaService) {
    slashCommand("next-all", "Shows when the next raid bosses are spawning.") {
        callback {
            respond {
                content = "_ _\n${l2AmerikaService.bossTimesToString()}"
            }
        }
    }
}

fun InteractionBuilder.subscribeEvents(channelService: ChannelService) {
    slashCommand(
        "subscribe-events",
        "Subscribes the channel to receive updates which and when events are starting."
    ) {
        val rawChannelId by channelParameter("channel", "Which channel to subscribe")
        val rawRoleId by roleParameter("announcement-role", "Which role to ping when a event starts")

        callback {
            respond {
                val channelId = ChannelId(rawChannelId!!)
                val roleId = RoleId(rawRoleId!!)
                content = if (!channelService.existsEvents(channelId)) {
                    channelService.addEventsChannel(channelId, roleId)
                    "Channel successfully subscribed"
                } else {
                    "Channel already subscribed"
                }
            }
        }
    }
}

fun InteractionBuilder.unsubscribeEvents(channelService: ChannelService) {
    slashCommand(
        "unsubscribe-events",
        "Unsubscribes the channel from receiving updates which and when events are starting."
    ) {
        val channelId by channelParameter("channel", "Which channel to unsubscribe")
        callback {
            respond {
                channelService.removeEventsChannel(ChannelId(channelId!!))
                content = "Unsubscribed from receiving updates"
            }
        }
    }

}

fun InteractionBuilder.subscribeRbs(channelService: ChannelService, l2AmerikaService: L2AmerikaService) {
    slashCommand("subscribe-rbs", "Subscribes the channel to receive updates when raid bosses are spawning.") {
        val rawChannelId by channelParameter("channel", "Which channel to subscribe")
        val rawRoleId by roleParameter("announcement-role", "Which role to ping when a rb spawns")

        callback {
            respond {
                val channelId = ChannelId(rawChannelId!!)
                val roleId = RoleId(rawRoleId!!)
                content = if (!channelService.existsRbs(channelId)) {
                    channelService.addRbChannel(channelId, roleId)
                    channelService.scheduleJobsForChannel(channelId, l2AmerikaService.bossesData.values.toSet())
                    "Channel successfully subscribed"
                } else {
                    "Channel already subscribed"
                }
            }
        }
    }
}

fun InteractionBuilder.unsubscribeRbs(channelService: ChannelService) {
    slashCommand(
        "unsubscribe-rbs",
        "Unsubscribes the channel from receiving updates when raid bosses are spawning."
    ) {
        val channelId by channelParameter("channel", "Which channel to unsubscribe")
        callback {
            respond {
                channelService.removeRbChannel(ChannelId(channelId!!))
                content = "Unsubscribed from receiving updates"
            }
        }
    }
}