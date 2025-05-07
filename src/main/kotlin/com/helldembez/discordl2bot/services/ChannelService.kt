package com.helldembez.discordl2bot.services

import arrow.core.getOrNone
import com.helldembez.discordl2bot.AMERIKA_BOSS_NAMES
import com.helldembez.discordl2bot.AMERIKA_BOSS_NAMES.SIEGE
import com.helldembez.discordl2bot.AMERIKA_BOSS_NAMES.TERRITORYWAR
import com.helldembez.discordl2bot.BOT_TOKEN
import com.helldembez.discordl2bot.CLOCK
import com.helldembez.discordl2bot.EventType
import com.helldembez.discordl2bot.UTC
import com.helldembez.discordl2bot.ZONE
import com.helldembez.discordl2bot.events
import com.helldembez.discordl2bot.timeUntil
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
    private val rbChannelsFile: File = File(File(System.getenv("CHANNELS_DIRECTORY")), "rb-channels.txt")
    private val eventsChannelsFile: File = File(File(System.getenv("CHANNELS_DIRECTORY")), "events-channels.txt")
    private val rbChannels = mutableMapOf<ChannelId, ChannelData>()
    private val eventsChannels = mutableMapOf<ChannelId, ChannelData>()
    private val jobs: MutableMap<ChannelId, List<Job>> = mutableMapOf()

    init {
        initFile(rbChannels, rbChannelsFile)
        initFile(eventsChannels, eventsChannelsFile)
        startEventAnnouncements()
    }

    private fun initFile(channelMap: MutableMap<ChannelId, ChannelData>, channelFile: File) {
        channelFile.createNewFile()
        channelFile.forEachLine { line ->
            val splittedLine = line.split(";")
            val channelId = ChannelId(splittedLine.first())
            val roleId = RoleId(splittedLine.last())
            channelMap[channelId] = ChannelData(channelId, roleId, ChannelClient(channelId.toString(), client))
        }
    }

    private fun startEventAnnouncements() {
        scope.launch {
            while (true) {
                val now = ZonedDateTime.now(UTC)
                val futureTasks = events.map {
                    it.registerTime.atDate(now.toLocalDate()).atZone(UTC) to it
                }.filter { (time, _) -> time.isAfter(now) }
                val tasksToExecute = futureTasks.ifEmpty {
                    events.map {
                        it.registerTime.atDate(now.toLocalDate().plusDays(1)).atZone(UTC) to it
                    }
                }

                for ((time, task) in tasksToExecute) {
                    val delayTime = time.timeUntil()
                    println("Next task scheduled at $time, waiting for $delayTime ms")
                    delay(delayTime)
                    println("Executing task '${task.type}' at $time")
                    eventsChannels.forEach { (_, data) ->
                        val unknownMsg = if (task.type == EventType.Unknown) {
                            "Let me know which event this was. ${task.registerTime.hour}"
                        } else {
                            ""
                        }
                        data.channelClient.sendMessage(
                            "Event ${task.type} starting in around 10 minutes <@&${data.roleId}> $unknownMsg"
                        )
                    }
                }
            }
        }
    }

    private fun addChannel(
        channelId: ChannelId,
        roleId: RoleId,
        channelMap: MutableMap<ChannelId, ChannelData>,
        channelFile: File
    ) {
        channelMap[channelId] = ChannelData(channelId, roleId, ChannelClient(channelId.toString(), client))
        val newLine = if (channelFile.length() > 0) {
            "\n"
        } else {
            ""
        }
        channelFile.appendText("$newLine$channelId;$roleId")
    }

    private fun removeChannel(channelId: ChannelId, channelMap: MutableMap<ChannelId, ChannelData>, channelFile: File) {
        channelMap.remove(channelId)
        cancelJobsForChannel(channelId)
        val filteredLines = channelFile.readLines().filterNot { it.contains(channelId.toString()) }
        channelFile.writeText(filteredLines.joinToString("\n"))
    }

    fun addRbChannel(channelId: ChannelId, roleId: RoleId) = addChannel(channelId, roleId, rbChannels, rbChannelsFile)

    fun removeRbChannel(channelId: ChannelId) = removeChannel(channelId, rbChannels, rbChannelsFile)

    fun addEventsChannel(channelId: ChannelId, roleId: RoleId) =
        addChannel(channelId, roleId, eventsChannels, eventsChannelsFile)

    fun removeEventsChannel(channelId: ChannelId) = removeChannel(channelId, eventsChannels, eventsChannelsFile)

    fun scheduleJobsForAllChannels(bossesData: Set<AmerikaBossData>) = bossesData.forEach(::scheduleJobForAllChannels)

    fun scheduleJobForAllChannels(bossData: AmerikaBossData) = rbChannels.forEach { (channelId, channelData) ->
        bossData.time.onSome { time ->
            addJobForChannel(channelId, createJobForChannel(channelData, bossData.name, time))
        }
    }

    fun scheduleJobsForChannel(channelId: ChannelId, bossesData: Set<AmerikaBossData>) {
        rbChannels.getOrNone(channelId).onSome { channelData ->
            bossesData.forEach {
                it.time.onSome { time ->
                    addJobForChannel(channelId, createJobForChannel(channelData, it.name, time))
                }
            }
        }
    }

    private fun addJobForChannel(channelId: ChannelId, job: Job) {
        val channelJobs = jobs.getOrDefault(channelId, listOf()) + job
        jobs[channelId] = channelJobs
    }

    private fun createJobForChannel(channelData: ChannelData, name: AMERIKA_BOSS_NAMES, time: ZonedDateTime) = scope.launch {
        log.info { "Scheduling $name for ${channelData.channelId} on $time" }
        val now = ZonedDateTime.now(ZONE)
        if (now.plusHours(1).isBefore(time)) {
            val reminderTime = time.minusHours(1)
            delay(reminderTime.toInstant().toEpochMilli() - now.toInstant().toEpochMilli())
        }
        val relativeTime = timestamp(time.toInstant().toKotlinInstant(), RELATIVE)
        val dateTime = timestamp(time.toInstant().toKotlinInstant(), LONG_DATE_TIME)
        val word = if (name() in arrayOf(TERRITORYWAR(), SIEGE())) {
            "starting"
        } else {
            "spawning"
        }
        channelData.channelClient.sendMessage(
            "${name()} is $word $relativeTime at $dateTime <@&${channelData.roleId}>",
            embeds = name.toEmbeds()
        )
        delay(time.toInstant().toEpochMilli() - Instant.now(CLOCK).toEpochMilli())
        channelData.channelClient.sendMessage(
            "${name()} is $word right now! <@&${channelData.roleId}>",
            embeds = name.toEmbeds()
        )
        log.info { "$name $word for ${channelData.channelId}" }
    }

    private fun cancelJobsForChannel(channelId: ChannelId) {
        jobs.getOrNone(channelId).onSome {
            it.forEach(Job::cancel)
        }
        jobs.remove(channelId)
    }

    fun existsRbs(channelId: ChannelId): Boolean = rbChannels.containsKey(channelId)
    fun existsEvents(channelId: ChannelId): Boolean = eventsChannels.containsKey(channelId)
}

data class ChannelId(val channelId: String) {
    override fun toString() = channelId
}

data class RoleId(val roleId: String) {
    override fun toString() = roleId
}

data class ChannelData(
    val channelId: ChannelId,
    val roleId: RoleId,
    val channelClient: ChannelClient,
)