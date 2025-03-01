package me.bumiller.civoris.rest

import io.ktor.client.request.*
import me.bumiller.civoris.test.ktorEndpointTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PingTest {

    @Test
    fun `GET ping returns 200`() = ktorEndpointTest { _, client ->
        val res = client.get("/test/api/ping/")

        assertEquals(200, res.status.value)
    }

}