package com.helldembez.discordl2bot

import arrow.core.getOrElse
import com.helldembez.discordl2bot.HTML_TAGS.TD
import com.helldembez.discordl2bot.services.BossData
import org.jsoup.nodes.Element
import java.time.ZonedDateTime

fun Set<BossData>.sort() =
    this.sortedBy { (_, value) -> value.getOrElse(ZonedDateTime::now) }

fun Element.containsBoss(bossNames: List<String>): Boolean {
    val elements = this.getElementsByTag(TD())
    return elements.size > 0 && bossNames.contains(elements[0].text())
}