package com.onsoim.keyword.digger.trend.infrastructure.google

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class GoogleTrendsRssParserTest {

    private val parser = GoogleTrendsRssParser()

    private fun loadSample(): String {
        val inputStream = javaClass.getResourceAsStream(
            "/google-trends-US.xml"
        ) ?: error("Google Trends RSS sample not found.")
    
        return inputStream.bufferedReader(Charsets.UTF_8).use {
            it.readText()
        }
    }
    
    @Test
    fun `Parses keyword metadata and related news`() {
        val result = parser.parse(loadSample())
        
        assertTrue(result.isNotEmpty())
        
        val first = result.first()
        
        assertEquals("nocturne series", first.keyword)
        assertEquals("1000+", first.approximateTraffic)
        
        assertEquals(
            "https://trends.google.com/trends/explore?q=nocturne+series",
            first.trendUrl
        )
        
        assertNotNull(first.startedAt)
        
        assertEquals(3, first.relatedNews.size)
        assertEquals("IMDb", first.relatedNews.first().source)
        
        assertEquals(
            "https://www.imdb.com/news/ni66098068/?ref_=nmnw_art_perm",
            first.relatedNews.first().url
        )
        
        assertTrue(result.all { it.keyword.isNotBlank() })
    }
    
    @Test
    fun `Rejects blank XML`() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse(" ")
        }
    }
    
    @Test
    fun `Rejects invalid RSS root`() {
        assertThrows(IllegalArgumentException::class.java) {
            parser.parse("<root/>")
        }
    }
}