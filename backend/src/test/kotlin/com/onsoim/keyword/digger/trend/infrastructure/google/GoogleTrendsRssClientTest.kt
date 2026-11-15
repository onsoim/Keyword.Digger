package com.onsoim.keyword.digger.trend.infrastructure.google

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Duration


@Tag("live")
class GoogleTrendsRssClientTest {

    private val client = GoogleTrendsRssClient(
        properties = GoogleTrendsRssProperties(
            baseUrl = "https://trends.google.com/trending/rss",
            connectTimeout = Duration.ofSeconds(5),
            requestTimeout = Duration.ofSeconds(15),
        )
    )

    @Test
    fun `Get Google Trends RSS`() {
        val xml = client.fetch()

        assertTrue(xml.isNotBlank(), "RSS response is blank.")
        assertTrue(xml.contains("<rss"), "Not RSS typed XML.")

        println("Length of Response: ${xml.length} characters")
    }
}