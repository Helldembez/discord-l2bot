@file:Suppress("VulnerableCodeUsages")

package com.helldembez.discordl2bot.services

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.none
import arrow.core.some
import arrow.core.toOption
import com.helldembez.discordl2bot.BOSS_NAMES
import com.helldembez.discordl2bot.BOSS_NAMES.*
import com.helldembez.discordl2bot.HTML_TAGS.*
import com.helldembez.discordl2bot.INTERVAL_DAYS
import com.helldembez.discordl2bot.INTERVAL_WEEKS
import com.helldembez.discordl2bot.REF_LINDVIOR
import com.helldembez.discordl2bot.REF_SIEGE
import com.helldembez.discordl2bot.REF_TERRITORYWAR
import com.helldembez.discordl2bot.UTC
import com.helldembez.discordl2bot.containsBoss
import com.helldembez.discordl2bot.sort
import com.jessecorbett.diskord.util.TimestampFormat.LONG_DATE_TIME
import com.jessecorbett.diskord.util.TimestampFormat.RELATIVE
import com.jessecorbett.diskord.util.timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import kotlinx.datetime.toKotlinInstant
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.ceil
import kotlin.math.max

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
        val now = ZonedDateTime.now(UTC)

        bossesData[LINDVIOR] = BossData(LINDVIOR, nextOccurrenceZoned(now, REF_LINDVIOR))
        bossesData[TERRITORYWAR] = BossData(TERRITORYWAR, nextOccurrenceZoned(now, REF_TERRITORYWAR))
        bossesData[SIEGE] = BossData(SIEGE, nextOccurrenceZoned(now, REF_SIEGE))
    }

    fun pollAliveBosses(scope: CoroutineScope, channelService: ChannelService) = scope.future {
        while (true) {
            val now = ZonedDateTime.now(UTC)
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
                        }.onNone {
                            val bossData = BossData(name, none())
                            bossesData.replace(name, bossData)
                        }
                    }

                if (filteredBossesData.containsKey(LINDVIOR)) {
                    bossesData[LINDVIOR] = BossData(LINDVIOR, nextOccurrenceZoned(now, REF_LINDVIOR))
                }
                if (filteredBossesData.containsKey(TERRITORYWAR)) {
                    bossesData[TERRITORYWAR] = BossData(TERRITORYWAR, nextOccurrenceZoned(now, REF_TERRITORYWAR))
                }
                if (filteredBossesData.containsKey(SIEGE)) {
                    bossesData[SIEGE] = BossData(SIEGE, nextOccurrenceZoned(now, REF_SIEGE))
                }
            }
            delay(1800000)
        }
    }

    private fun getRbs(): List<Element> {
        val doc = Jsoup.connect("https://www.l2amerika.com/?page=boss-status").get()
        val tables = doc.body().getElementsByTag(TABLE())
        return tables[0].getElementsByTag(TR()) + tables[1].getElementsByTag(TR())
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

    fun nextOccurrenceZoned(now: ZonedDateTime, refZoned: ZonedDateTime): Option<ZonedDateTime> {
        val refUtc = refZoned.withZoneSameInstant(ZoneOffset.UTC)
        val nowUtc = now.withZoneSameInstant(ZoneOffset.UTC)
        val duration = Duration.between(refUtc.toInstant(), nowUtc.toInstant())
        val periodsExact = duration.seconds.toDouble() / Duration.ofDays(INTERVAL_DAYS).seconds.toDouble()
        val k = max(0L, ceil(periodsExact).toLong())
        var candidateUtc = refUtc.plusWeeks(k * INTERVAL_WEEKS)
        if (!candidateUtc.isAfter(nowUtc)) candidateUtc = candidateUtc.plusWeeks(INTERVAL_WEEKS)
        return candidateUtc.toOption()
    }
}

data class BossData(
    val name: BOSS_NAMES,
    val time: Option<ZonedDateTime>,
)