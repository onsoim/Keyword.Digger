package com.onsoim.keyword.digger.trend.infrastructure.google

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class GoogleTrendsRssParserTest {

    private val parser = GoogleTrendsRssParser()

    @Test
    fun `Extract infos from Google Trends RSS`() {
        val inputStream = javaClass.getResourceAsStream(
            "/google-trends-US.xml"
        ) ?: error("Google Trends RSS sample not found.")

        val xml = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        val result = parser.parse(xml)

        assertTrue(result.isNotEmpty(), "Keyword is not extracted.")
        println("Parserd keyword count: ${result.size}")

        result.forEach { trend ->
            assertTrue(trend.keyword.isNotBlank())
            println("Keyword: ${trend.keyword}")
            println("Traffic: ${trend.approximateTraffic}")
        }
    }
}