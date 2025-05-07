package com.helldembez.discordl2bot.services

import org.junit.Before
import org.junit.Test

class L2ExiliumServiceTest {

    private lateinit var service: L2ExiliumService

    @Before
    fun setUp() {
        service = L2ExiliumService()
    }

    @Test
    fun test() {
        val result = service.initBossesData()

        println(result)
    }

}