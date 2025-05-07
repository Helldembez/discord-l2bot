package com.helldembez.discordl2bot.services

import com.helldembez.discordl2bot.UTC
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.GlobalScope
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.test.assertEquals

private val log = KotlinLogging.logger {}

class L2AmerikaServiceTest {

    @MockK
    private lateinit var channelService: ChannelService

    private lateinit var service: L2AmerikaService

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        service = L2AmerikaService(GlobalScope, channelService)
    }

    @Test
    fun `nextLindvior right before it spawns`() {
        val zoned = ZonedDateTime.of(2024, 12, 1,18,11,27, 910009882, UTC)

        val result = service.nextLindvior(zoned, 48)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 1), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextLindvior right after it spawned`() {
        val zoned = ZonedDateTime.of(2024, 12, 1,19,11,27, 910009882, UTC)

        val result = service.nextLindvior(zoned, 48)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 15), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextLindvior 1 week before it spawns`() {
        val zoned = ZonedDateTime.of(2024, 11, 24,19,11,27, 910009882, UTC)

        val result = service.nextLindvior(zoned, 47)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 1), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }

    // now: 2024-12-07T21:11:43.408997034Z[UTC] current weeknumber: 49 *
    // NextSunday: 2024-12-07T22:00+01:00[Europe/Amsterdam], result: Option.Some(2024-12-14T22:00+01:00[Europe/Amsterdam]) *
    @Test
    fun `nextTw right before it starts`() {
        val zoned = ZonedDateTime.of(2024, 12, 7,20,11,43, 408997034, UTC)

        val result = service.nextTw(zoned, 49)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 7), LocalTime.of(21, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextTw right after it started`() {
        val zoned = ZonedDateTime.of(2024, 12, 7,21,11,43, 408997034, UTC)

        val result = service.nextTw(zoned, 49)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 21), LocalTime.of(21, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextTw 1 week before it starts`() {
        val zoned = ZonedDateTime.of(2024, 12, 1,21,11,43, 408997034, UTC)

        val result = service.nextTw(zoned, 48)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 7), LocalTime.of(21, 0), UTC), result.getOrNull()!!)
    }

    // now: 2024-12-08T19:11:47.752807162Z[UTC] current weeknumber: 49 *
    // NextSunday: 2024-12-08T20:00+01:00[Europe/Amsterdam], result: Option.Some(2024-12-15T20:00+01:00[Europe/Amsterdam]) *
    @Test
    fun `nextSiege right before it starts`() {
        val zoned = ZonedDateTime.of(2024, 12, 8,18,11,47, 752807162, UTC)

        val result = service.nextSiege(zoned, 49)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 8), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextSiege right after it started`() {
        val zoned = ZonedDateTime.of(2024, 12, 8,19,11,47, 752807162, UTC)

        val result = service.nextSiege(zoned, 49)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 22), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }

    @Test
    fun `nextSiege 1 week before it starts`() {
        val zoned = ZonedDateTime.of(2024, 12, 1,19,11,47, 752807162, UTC)

        val result = service.nextSiege(zoned, 48)

        log.info { "test: $result"}
        println(result)
        assertEquals(ZonedDateTime.of(LocalDate.of(2024, 12, 8), LocalTime.of(19, 0), UTC), result.getOrNull()!!)
    }
}