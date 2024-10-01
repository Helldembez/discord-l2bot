@file:Suppress("VulnerableCodeUsages")

package com.helldembez.discordl2bot.services

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import com.helldembez.discordl2bot.*
import com.helldembez.discordl2bot.BOSS_NAMES.*
import com.helldembez.discordl2bot.HTML_TAGS.*
import com.jessecorbett.diskord.util.TimestampFormat.LONG_DATE_TIME
import com.jessecorbett.diskord.util.TimestampFormat.RELATIVE
import com.jessecorbett.diskord.util.timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.toKotlinInstant
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.WeekFields
import java.util.*

private val FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
private val L2AMERIKA_ZONE = ZoneId.of("America/Sao_Paulo")

class L2AmerikaService(
    private val scope: CoroutineScope,
    private val channelService: ChannelService
) {
    val bossesData = mutableMapOf<BOSS_NAMES, BossData>()

    fun init(): L2AmerikaService {
        initBossesData()
        initEvents()
        channelService.scheduleJobsForAllChannels(bossesData.values.toSet())
        pollAliveBosses(scope, channelService)
        return this
    }

    private fun initBossesData() {
        val rbs = getRbs()
        rbs.filter { it.containsBoss(enumValues<BOSS_NAMES>().map(BOSS_NAMES::invoke)) }.forEach { row ->
            val columns = row.getElementsByTag(TD())
            val name = BOSS_NAMES.fromBossName(columns[0].text())
            val time = columns[3].text()
            val parsedTime = parseDate(time)
            bossesData[name] = BossData(name, parsedTime)
        }
    }

    private fun initEvents() {
        val now = ZonedDateTime.now(ZONE)
        val weekNumber = currentWeekNumber(now)

        bossesData[LINDVIOR] = BossData(LINDVIOR, nextLindvior(now, weekNumber))
        bossesData[TERRITORYWAR] = BossData(TERRITORYWAR, nextTw(now, weekNumber))
        bossesData[SIEGE] = BossData(SIEGE, nextSiege(now, weekNumber))
    }

    fun pollAliveBosses(scope: CoroutineScope, channelService: ChannelService) = scope.future {
        while (true) {
            val now = ZonedDateTime.now(ZONE)
            val filteredBossesData =
                bossesData.filter { (_, it) ->
                    it.time.isNone() || it.time.map { time -> time.isBefore(now) }.getOrElse { false }
                }
            if (filteredBossesData.isNotEmpty()) {
                val rbs = getRbs()
                rbs.filter { element -> element.containsBoss(filteredBossesData.map { it.value.name() }) }
                    .forEach { row ->
                        val columns = row.getElementsByTag(TD())
                        val name = BOSS_NAMES.fromBossName(columns[0].text())
                        val time = columns[3].text()
                        val parsedTime = parseDate(time)
                        parsedTime.onSome {
                            val bossData = BossData(name, parsedTime)
                            bossesData.replace(name, bossData)
                            channelService.scheduleJobForAllChannels(bossData)
                        }
                    }

                val weekNumber = currentWeekNumber(now)
                if (filteredBossesData.containsKey(LINDVIOR)) {
                    bossesData[LINDVIOR] = BossData(LINDVIOR, nextLindvior(now, weekNumber))
                }
                if (filteredBossesData.containsKey(TERRITORYWAR)) {
                    bossesData[TERRITORYWAR] = BossData(TERRITORYWAR, nextTw(now, weekNumber))
                }
                if (filteredBossesData.containsKey(SIEGE)) {
                    bossesData[SIEGE] = BossData(SIEGE, nextSiege(now, weekNumber))
                }
            }
            delay(1800000)
        }
    }

    private fun getRbs(): List<Element> {
        val doc = Jsoup.connect("https://www.l2amerika.com/?page=rankings").get()
        val tables = doc.body().getElementsByTag(TABLE())
        return tables[6].getElementsByTag(TR()) + tables[7].getElementsByTag(TR())
    }

    fun bossTimesToString() =
        bossesData.values.toSet().sort().joinToString(separator = "\n") { (name, time) -> bossTimeString(name(), time) }

    fun bossTimeToString() =
        bossesData.values.toSet().sort().first().let { (name, time) -> bossTimeString(name(), time) }

    private fun bossTimeString(name: String, time: Option<ZonedDateTime>) = time.fold(
        { "is ALIVE" },
        {
            val instant = it.toInstant().toKotlinInstant()
            val relativeTime = timestamp(instant, RELATIVE)
            val dateTime = timestamp(instant, LONG_DATE_TIME)
            val word = if (name in arrayOf(TERRITORYWAR(), SIEGE())) {
                "starts"
            } else {
                "spawns"
            }
            "$word $relativeTime at $dateTime"
        }
    ).let { "$name $it" }


    private fun parseDate(input: String): Option<ZonedDateTime> = if (input == "-") {
        none()
    } else {
        try {
            ZonedDateTime.of(LocalDateTime.parse(input, FORMATTER), L2AMERIKA_ZONE).some()
        } catch (e: DateTimeParseException) {
            none()
        }
    }

    fun currentWeekNumber(now: ZonedDateTime) =
        now.get(WeekFields.of(LOCALE).weekOfWeekBasedYear())

    fun nextLindvior(now: ZonedDateTime, weekNumber: Int): Option<ZonedDateTime> {
        val nextSunday =
            now.with(DayOfWeek.SUNDAY).withHour(21).withMinute(0).withSecond(0).withNano(0).withZoneSameInstant(ZONE)
        return if (weekNumber % 2 != 0) {
            nextSunday.plusWeeks(1).some()
        } else if (nextSunday.isBefore(now)) {
            nextSunday.plusWeeks(2).some()
        } else {
            nextSunday.some()
        }
    }

    fun nextTw(now: ZonedDateTime, weekNumber: Int): Option<ZonedDateTime> {
        val nextSaturday =
            now.with(DayOfWeek.SATURDAY).withHour(23).withMinute(0).withSecond(0).withNano(0).withZoneSameInstant(ZONE)
        return if (weekNumber % 2 == 0 || nextSaturday.isBefore(now)) {
            nextSaturday.plusWeeks(1).some()
        } else if (nextSaturday.isBefore(now)) {
            nextSaturday.plusWeeks(2).some()
        } else {
            nextSaturday.some()
        }
    }

    fun nextSiege(now: ZonedDateTime, weekNumber: Int): Option<ZonedDateTime> {
        val nextSunday =
            now.with(DayOfWeek.SUNDAY).withHour(21).withMinute(0).withSecond(0).withNano(0).withZoneSameInstant(ZONE)
        return if (weekNumber % 2 == 0 || nextSunday.isBefore(now)) {
            nextSunday.plusWeeks(1).some()
        } else if (nextSunday.isBefore(now)) {
            nextSunday.plusWeeks(2).some()
        } else {
            nextSunday.some()
        }
    }

}

data class BossData(
    val name: BOSS_NAMES,
    val time: Option<ZonedDateTime>,
)