package com.onsoim.keyword.digger.trend.infrastructure.google

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.atomic.AtomicReference


class GoogleTrendsRssClientIntegrationTest {
    
    @Test
    fun `Fetches RSS from configured HTTP server`() {
        val responseBody = """<?xml version="1.0"?><rss version="2.0"></rss>"""

        val receivedPath = AtomicReference<String?>()
        val receivedQuery = AtomicReference<String?>()
        val receivedMethod = AtomicReference<String?>()
        
        val server = HttpServer.create(
            InetSocketAddress("127.0.0.1", 0),
            0
        )
        
        server.createContext("/trending/rss") { exchange ->
            receivedPath.set(exchange.requestURI.path)
            receivedQuery.set(exchange.requestURI.rawQuery)
            receivedMethod.set(exchange.requestMethod)
            
            val bytes = responseBody.toByteArray(StandardCharsets.UTF_8)
            
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            
            exchange.responseBody.use { output ->
                output.write(bytes)
            }
        }
        
        server.start()
        
        try {
            val properties = GoogleTrendsRssProperties(
                baseUrl = "http://127.0.0.1:${server.address.port}/trending/rss",
                connectTimeout = Duration.ofSeconds(1),
                requestTimeout = Duration.ofSeconds(2),
            )
            
            val client = GoogleTrendsRssClient(properties)
            
            val result = client.fetch("US")
            
            assertEquals(responseBody, result)
            assertEquals("GET", receivedMethod.get())
            assertEquals("/trending/rss", receivedPath.get())
            assertEquals("geo=US", receivedQuery.get())

        } finally {
            server.stop(0)
        }
    }
}