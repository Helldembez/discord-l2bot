package com.helldembez.discordl2bot

import arrow.core.firstOrNone
import arrow.core.toOption
import com.jessecorbett.diskord.api.channel.ChannelClient
import com.jessecorbett.diskord.api.common.Channel
import com.jessecorbett.diskord.api.common.GuildTextChannel
import com.jessecorbett.diskord.api.common.Message
import com.jessecorbett.diskord.bot.BotBase
import com.jessecorbett.diskord.bot.EventDispatcherWithContext
import com.jessecorbett.diskord.bot.events
import com.jessecorbett.diskord.internal.client.RestClient
import com.jessecorbett.diskord.util.isFromUser
import kotlin.random.Random

private val client = RestClient.default(BOT_TOKEN)

fun BotBase.bindEvents() {
    events {
        onInit {
            it.guilds.forEach { guild ->
                GUILD_IDS.add(guild.id)
            }
        }
        onMessageUpdate { maxReply(this, it) }
        onMessageCreate {
            maxReply(this, it)
            adminCommands(this, it)
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

suspend fun adminCommands(context: EventDispatcherWithContext, message: Message) {
    with(context) {
        if (message.isFromUser && message.guild?.guildId == "934967304788258906") {
            val command = message.content.split(" ")
            when (command.firstOrNull()) {
                "!list-guilds" -> {
                    val response = GUILD_IDS.map {
                        val guild = guild(it)
                        "$it:${guild.getGuild().name}:${guild.getChannels().size}:${guild.getGuild(true).approximateMemberCount}"
                    }.joinToString(separator = "\n", prefix = "Guilds:\n")
                    message.respond(response)
                }
                "!leave-guild" -> {
                    GUILD_IDS.firstOrNone{ guildId ->
                        message.content.contains(guildId)
                    }.onSome {
                        val guild = guild(it)
                        message.respond("leaving guild: '${guild.getGuild().name}'.")
                        guild.leave()
                    }
                }
                "!channels-guild" -> {
                    GUILD_IDS.firstOrNone{ guildId ->
                        message.content.contains(guildId)
                    }.onSome {
                        val guild = guild(it)
                        val channels = guild.getChannels().mapNotNull { channel ->
                            when (channel) {
                                is GuildTextChannel -> "${channel.name}(${channel.id})"
                                else -> null
                            }
                        }
                        message.respond(channels.joinToString(separator = "\n", prefix = "Channels in guild '${guild.getGuild().name}'(${guild.guildId}):\n"))

                    }.onNone { message.respond("Guild not found.") }
                }
                "!members-guild" -> {
                    GUILD_IDS.firstOrNone{ guildId ->
                        message.content.contains(guildId)
                    }.onSome {
                        val guild = guild(it)
                        val memberCount = guild.getGuild().approximateMemberCount ?: 10
                        val members = guild.getMembers(memberCount).map { member ->
                            "'${member.nickname}'-'${member.user?.username}'(${member.user?.id})"
                        }
                        message.respond(members.joinToString(separator = "\n", prefix = "Members in guild '${guild.getGuild().name}'(${guild.guildId}):\n"))

                    }.onNone { message.respond("Guild not found.") }
                }
                "!read-channel" -> {
                    val channelId = command[1]
                    val limit = command.getOrNull(2).orEmpty().toIntOrNull() ?: 10
                    val channelClient = ChannelClient(channelId, client)
                    val messages = channelClient.getMessages(limit).map { "${it.author.username}(${it.author.displayName})(${it.id})> ${it.content}" }

                    try {
                        message.respond(messages.joinToString(separator = "\n", prefix = "Messages in channel '${(channelClient.getChannel() as GuildTextChannel).name}':\n"))
                    } catch (e: Exception) {
                        message.respond("Error reading channel messages: ${e.message}")
                    }
                }
                "!read-before" -> {
                    val channelId = command[1]
                    val limit = command.getOrNull(2).orEmpty().toIntOrNull() ?: 10
                    command.getOrNull(3).toOption().onSome { messageId ->
                        val channelClient = ChannelClient(channelId, client)
                        val messages = channelClient.getMessagesBefore(limit, messageId).map { "${it.sentAt}${it.author.username}(${it.author.displayName})(${it.id})> ${it.content}" }

                        try {
                            message.respond(messages.joinToString(separator = "\n", prefix = "Messages in channel '${(channelClient.getChannel() as GuildTextChannel).name}':\n"))
                        } catch (e: Exception) {
                            message.respond("Error reading channel messages: ${e.message}")
                        }
                    }.onNone { message.respond("Missing message id.") }

                }
            }

        }
    }
}