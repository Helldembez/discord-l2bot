package com.helldembez.discordl2bot.services

import arrow.core.getOrNone
import com.helldembez.discordl2bot.BOSS_NAMES.SIEGE
import com.helldembez.discordl2bot.BOSS_NAMES.TERRITORYWAR
import com.helldembez.discordl2bot.BOT_TOKEN
import com.helldembez.discordl2bot.CLOCK
import com.helldembez.discordl2bot.ZONE
import com.jessecorbett.diskord.api.channel.ChannelClient
import com.jessecorbett.diskord.internal.client.RestClient
import com.jessecorbett.diskord.util.TimestampFormat.LONG_DATE_TIME
import com.jessecorbett.diskord.util.TimestampFormat.RELATIVE
import com.jessecorbett.diskord.util.sendMessage
import com.jessecorbett.diskord.util.timestamp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.toKotlinInstant
import java.io.File
import java.time.Instant
import java.time.ZonedDateTime

private val client = RestClient.default(BOT_TOKEN)
private val log = KotlinLogging.logger {}

class ChannelService(private val scope: CoroutineScope) {
    private val channelsFile: File = File(File(System.getenv("CHANNELS_DIRECTORY")), "channels.txt")
    private val channels = mutableMapOf<ChannelId, ChannelData>()
    private val jobs: MutableMap<ChannelId, List<Job>> = mutableMapOf()

    init {
        channelsFile.createNewFile()
        channelsFile.forEachLine { line ->
            val channelId = ChannelId(line)
            channels[channelId] = ChannelData(channelId, ChannelClient(channelId.toString(), client))
        }
    }

    fun addChannel(channelId: ChannelId) {
        channels[channelId] = ChannelData(channelId, ChannelClient(channelId.toString(), client))
        val newLine = if (channelsFile.length() > 0) {
            "\n"
        } else {
            ""
        }
        channelsFile.appendText("$newLine$channelId")
    }

    fun removeChannel(channelId: ChannelId) {
        channels.remove(channelId)
        cancelJobsForChannel(channelId)
        val filteredLines = channelsFile.readLines().filterNot { it.contains(channelId.toString()) }
        channelsFile.writeText(filteredLines.joinToString("\n"))
    }

    fun scheduleJobsForAllChannels(bossesData: Set<BossData>) = bossesData.forEach(::scheduleJobForAllChannels)

    fun scheduleJobForAllChannels(bossData: BossData) = channels.forEach { (channelId, channelData) ->
        bossData.time.onSome { time ->
            addJobForChannel(channelId, createJobForChannel(channelData.channelClient, bossData.name, time))
        }
    }

    fun scheduleJobsForChannel(channelId: ChannelId, bossesData: Set<BossData>) {
        channels.getOrNone(channelId).onSome { channelData ->
            bossesData.forEach {
                it.time.onSome { time ->
                    addJobForChannel(channelId, createJobForChannel(channelData.channelClient, it.name, time))
                }
            }
        }
    }

    private fun addJobForChannel(channelId: ChannelId, job: Job) {
        val channelJobs = jobs.getOrDefault(channelId, listOf()) + job
        jobs[channelId] = channelJobs
    }

    private fun createJobForChannel(channel: ChannelClient, name: String, time: ZonedDateTime) = scope.launch {
        log.info { "Scheduling $name for ${channel.channelId} on $time" }
        val now = ZonedDateTime.now(ZONE)
        if (now.plusHours(1).isBefore(time)) {
            val reminderTime = time.minusHours(1)
            delay(reminderTime.toInstant().toEpochMilli() - now.toInstant().toEpochMilli())
        }
        val relativeTime = timestamp(time.toInstant().toKotlinInstant(), RELATIVE)
        val dateTime = timestamp(time.toInstant().toKotlinInstant(), LONG_DATE_TIME)
        val word = if (name in arrayOf(TERRITORYWAR(), SIEGE())) { "starting" } else { "spawning" }
        channel.sendMessage("$name is $word $relativeTime at $dateTime")
        delay(time.toInstant().toEpochMilli() - Instant.now(CLOCK).toEpochMilli())
        channel.sendMessage("$name is $word right now!")
        log.info { "$name $word for ${channel.channelId}" }
    }

    private fun cancelJobsForChannel(channelId: ChannelId) {
        jobs.getOrNone(channelId).onSome {
            it.forEach(Job::cancel)
        }
        jobs.remove(channelId)
    }

    fun exists(channelId: ChannelId): Boolean = channels.containsKey(channelId)
}

data class ChannelId(val channelId: String) {
    override fun toString() = channelId
}

data class ChannelData(
    val channelId: ChannelId,
    val channelClient: ChannelClient,
)