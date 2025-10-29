package com.helldembez.discordl2bot.services

import arrow.core.getOrElse
import com.helldembez.discordl2bot.REF_LINDVIOR
import com.helldembez.discordl2bot.REF_SIEGE
import com.helldembez.discordl2bot.REF_TERRITORYWAR
import com.helldembez.discordl2bot.UTC
import com.helldembez.discordl2bot.ZONE
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import java.time.ZonedDateTime
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Unit tests for the `nextOccurrenceZoned` method of the L2AmerikaService class.
 * This method calculates the next occurrence of an event based on a reference time
 * and the current time, adjusted by predefined intervals.
 */

class L2AmerikaServiceTest {
    @MockK
    private lateinit var channelService: ChannelService

    private lateinit var service: L2AmerikaService

    @BeforeTest
    fun setup() {
        MockKAnnotations.init(this)
        service = L2AmerikaService(scope = CoroutineScope(EmptyCoroutineContext), channelService = channelService)
    }

    @Test
    fun `nextOccurrenceZoned calculates the next occurrence correctly before DST change`() {
        val now = ZonedDateTime.of(2025, 10, 18, 20, 0, 0, 0, UTC) // Before REF_LINDVIOR
        val expected = ZonedDateTime.of(2025, 10, 19, 21, 0, 0, 0, ZONE)

        val result = service.nextOccurrenceZoned(now, REF_LINDVIOR).getOrElse { error("Unexpected error") }

        assertTrue { result.isEqual(expected) }
    }

    @Test
    fun `nextOccurrenceZoned calculates the next occurrence correctly after DST change for Lindvior`() {
        val now = ZonedDateTime.of(2025, 10, 20, 20, 0, 0, 0, UTC) // After REF_LINDVIOR
        val expected = ZonedDateTime.of(2025, 11, 2, 20, 0, 0, 0, ZONE)

        val result = service.nextOccurrenceZoned(now, REF_LINDVIOR).getOrElse { error("Unexpected error") }

        assertTrue { result.isEqual(expected) }
    }

    @Test
    fun `nextOccurrenceZoned calculates the next occurrence correctly before DST change for Territory War`() {
        val now = ZonedDateTime.of(2025, 10, 24, 22, 0, 0, 0, UTC) // Before REF_TERRITORYWAR
        val expected = ZonedDateTime.of(2025, 10, 25, 23, 0, 0, 0, ZONE)

        val result = service.nextOccurrenceZoned(now, REF_TERRITORYWAR).getOrElse { error("Unexpected error") }

        assertTrue { result.isEqual(expected) }
    }

    @Test
    fun `nextOccurrenceZoned calculates the next occurrence correctly after DST change for Siege`() {
        val now = ZonedDateTime.of(2025, 10, 27, 20, 0, 0, 0, UTC) // After REF_SIEGE
        val expected = ZonedDateTime.of(2025, 11, 9, 20, 0, 0, 0, ZONE)

        val result = service.nextOccurrenceZoned(now, REF_SIEGE).getOrElse { error("Unexpected error") }

        assertTrue { result.isEqual(expected) }
    }
}