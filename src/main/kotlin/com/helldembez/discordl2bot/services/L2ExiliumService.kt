package com.helldembez.discordl2bot.services

import arrow.core.getOrElse
import arrow.core.none
import arrow.core.toOption
import com.helldembez.discordl2bot.AMERIKA_BOSS_NAMES
import com.helldembez.discordl2bot.AMERIKA_BOSS_NAMES.*
import com.helldembez.discordl2bot.EXILIUM_BOSS_NAMES
import com.helldembez.discordl2bot.HTML_TAGS.*
import com.helldembez.discordl2bot.UTC
import com.helldembez.discordl2bot.ZONE
import com.helldembez.discordl2bot.containsBoss
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.ZonedDateTime

class L2ExiliumService(
    private val scope: CoroutineScope,
    private val channelService: ChannelService
) {
    val bossesData = mutableMapOf<EXILIUM_BOSS_NAMES, ExiliumBossData>()

    fun init(): L2ExiliumService {
        initBossesData()
        channelService.scheduleJobsForAllChannels(bossesData.values.toSet())
        pollAliveBosses(scope, channelService)
        return this
    }

    fun initBossesData() {
        getRbs().filter { it.containsBoss(enumValues<EXILIUM_BOSS_NAMES>().map(EXILIUM_BOSS_NAMES::invoke)) }.forEach { row ->
            val columns = row.getElementsByTag(TD())
            val name = EXILIUM_BOSS_NAMES.fromBossName(columns[0].text())
            val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(columns[3].firstElementChild()!!.attributes().get("time").toLong()), ZONE)
            bossesData[name] = ExiliumBossData(name, time.toOption())
            println("$name $time")
        }
        getSiegeTw().forEach { (name, element) ->
            val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(element.attributes().get("time").toLong()), ZONE)
            bossesData[name] = ExiliumBossData(name, time.toOption())
            println("$name $time")
        }
    }

    fun getRbs(): List<Element> {
        val doc = Jsoup.connect("https://www.exiliumworld.com/servers/faris/boss").get()
        val tables = doc.body().getElementsByTag(TABLE())
        return tables[0].getElementsByTag(TR())
    }

    fun getSiegeTw(): List<Pair<EXILIUM_BOSS_NAMES, Element>> {
        val doc = Jsoup.connect("https://www.exiliumworld.com/servers/faris/territory_status").get()
        val row = doc.getElementsByClass("row")[1]
        val tw = row.getElementsMatchingText("Next Territory War:").last()!!.lastElementSibling()
        val siege = row.getElementsMatchingText("Next Siege:").last()!!.lastElementSibling()

        return listOf(EXILIUM_BOSS_NAMES.TERRITORYWAR to tw, EXILIUM_BOSS_NAMES.SIEGE to siege)
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
                        val name = AMERIKA_BOSS_NAMES.fromBossName(columns[0].text())
                        val time = columns[3].text()
                        val parsedTime = parseDate(time)
                        parsedTime.onSome {
                            val bossData = AmerikaBossData(name, parsedTime)
                            bossesData.replace(name, bossData)
                            channelService.scheduleJobForAllChannels(bossData)
                        }.onNone {
                            val bossData = AmerikaBossData(name, none())
                            bossesData.replace(name, bossData)
                        }
                    }

                val weekNumber = currentWeekNumber(now)
                if (filteredBossesData.containsKey(LINDVIOR)) {
                    bossesData[LINDVIOR] = AmerikaBossData(LINDVIOR, nextLindvior(now, weekNumber))
                }
                if (filteredBossesData.containsKey(TERRITORYWAR)) {
                    bossesData[TERRITORYWAR] = AmerikaBossData(TERRITORYWAR, nextTw(now, weekNumber))
                }
                if (filteredBossesData.containsKey(SIEGE)) {
                    bossesData[SIEGE] = AmerikaBossData(SIEGE, nextSiege(now, weekNumber))
                }
            }
            delay(1800000)
        }
    }
}