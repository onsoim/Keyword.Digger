package com.onsoim.keyword.digger.trend.infrastructure.google

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test


@Tag("live")
class GoogleTrendsRssClientTest {

    private val client = GoogleTrendsRssClient()

    @Test
    fun `Get Google Trends RSS`() {
        val xml = client.fetch()

        assertTrue(xml.isNotBlank(), "RSS response is blank.")
        assertTrue(xml.contains("<rss"), "Not RSS typed XML.")

        println("Length of Response: ${xml.length} characters")
    }
}