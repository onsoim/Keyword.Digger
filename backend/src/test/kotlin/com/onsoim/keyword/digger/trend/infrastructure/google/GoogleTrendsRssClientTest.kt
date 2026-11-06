package com.onsoim.keyword.digger.trend.infrastructure.google

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path


class GoogleTrendsRssClientTest {

    private val client = GoogleTrendsRssClient()

    @Test
    fun `Get Google Trends RSS`() {
        val xml = client.fetch()

        assertTrue(xml.isNotBlank(), "RSS response is blank.")
        assertTrue(xml.contains("<rss"), "Not RSS typed XML.")

        val file = Path.of(
            "src",
            "test",
            "resources",
            "google-trends-US.xml"
        )

        Files.writeString(
            file,
            xml,
            StandardCharsets.UTF_8
        )

        println("Saved RSS: ${file.toAbsolutePath()}")
        println("Lenth of Response: ${xml.length} characters")
    }
}