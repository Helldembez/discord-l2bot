package com.helldembez.discordl2bot

import com.helldembez.discordl2bot.services.ChannelId
import com.helldembez.discordl2bot.services.ChannelService
import com.helldembez.discordl2bot.services.L2AmerikaService
import com.helldembez.discordl2bot.services.RoleId
import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.api.interaction.ApplicationCommand
import com.jessecorbett.diskord.bot.EventDispatcherWithContext
import com.jessecorbett.diskord.bot.interaction.CommandContext
import com.jessecorbett.diskord.util.isFromUser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

private val log = KotlinLogging.logger {}

class CommandsOrchestrator(scope: CoroutineScope) {
    private val channelService = ChannelService(scope)
    private val l2AmerikaService = L2AmerikaService(scope, channelService).init()

    fun next(context: CommandContext<ApplicationCommand>) {
        with(context) {
            callback {
                respond {
                    embeds = l2AmerikaService.bossesData.values.toSet().sort().first().name.toEmbeds().toList()
                    content = "_ _\n${l2AmerikaService.bossTimeToString()}"
                }
            }
        }
    }
    fun nextAll(context: CommandContext<ApplicationCommand>) {
        with(context) {
            callback {
                respond {
                    content = "_ _\n${l2AmerikaService.bossTimesToString()}"
                }
            }
        }
    }
    fun registerChannel(context: CommandContext<ApplicationCommand>) {
        with(context) {
            val rawChannelId  by channelParameter("channel", "Which channel to subscribe")
            val rawRoleId by roleParameter("announcement-role", "Which role to ping when a rb spawns")
            val channelId = ChannelId(rawChannelId!!)
            val roleId = RoleId(rawRoleId!!)

            callback {
                respond {
                    content = if (!channelService.exists(channelId)) {
                        channelService.addChannel(channelId, roleId)
                        channelService.scheduleJobsForChannel(channelId, l2AmerikaService.bossesData.values.toSet())
                        "Channel successfully subscribed"
                    } else {
                        "Channel already subscribed"
                    }
                }
            }
        }

    }

    fun unRegisterChannel(context: CommandContext<ApplicationCommand>) {
        with(context) {
            val channelId by channelParameter("channel", "Which channel to unsubscribe")
            callback {
                respond {
                    channelService.removeChannel(ChannelId(channelId!!))
                    content = "Unsubscribed from receiving updates"
                }
            }
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

//    fun subscribeEvents(context: CommandContext<ApplicationCommand>) {
//        with(context) {
//            val channelId by channelParameter("channel", "Which channel to subscribe")
//            val roleId by roleParameter("announcement-role", "Which role to ping when a rb spawns")
//            callback {
//                respond {
//                    content = if (!commandsOrchestrator.registerChannel(ChannelId(channelId!!), RoleId(roleId!!))) {
//                        "Channel already subscribed"
//                    } else {
//                        "Channel successfully subscribed"
//                    }
//                }
//            }
//        }
//
//    }
}