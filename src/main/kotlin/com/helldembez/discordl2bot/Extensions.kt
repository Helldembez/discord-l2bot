package com.helldembez.discordl2bot

import arrow.core.getOrElse
import com.helldembez.discordl2bot.HTML_TAGS.TD
import com.helldembez.discordl2bot.services.AmerikaBossData
import org.jsoup.nodes.Element
import java.time.Duration
import java.time.ZonedDateTime

fun Set<AmerikaBossData>.sort() =
    this.sortedBy { (_, value) -> value.getOrElse(ZonedDateTime::now) }

fun Element.containsBoss(bossNames: List<String>): Boolean {
    val elements = this.getElementsByTag(TD())
    return elements.size > 0 && bossNames.contains(elements[0].text())
}

fun ZonedDateTime.timeUntil(): Long {
    val now = ZonedDateTime.now(ZONE)
    return if (this.isAfter(now)) {
        Duration.between(now, this).toMillis()
    } else {
        Duration.between(now, this.plusDays(1)).toMillis()
    }
}
