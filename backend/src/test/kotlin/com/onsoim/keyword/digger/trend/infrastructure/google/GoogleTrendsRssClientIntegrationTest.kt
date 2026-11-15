package com.onsoim.keyword.digger.trend.infrastructure.google

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.net.http.HttpTimeoutException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger
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
    
    @Test
    fun `Rejects HTTP 4xx and 5xx responses`() {
        listOf(400, 500).forEach { statusCode ->
            
            val server = HttpServer.create(
                InetSocketAddress("127.0.0.1", 0),
                0
            )
            
            val responseBody = "Error response".toByteArray(StandardCharsets.UTF_8)

            server.createContext("/trending/rss") { exchange ->
                exchange.sendResponseHeaders(
                    statusCode,
                    responseBody.size.toLong()
                )
                
                exchange.responseBody.use { output ->
                    output.write(responseBody)
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
                
                val exception = assertThrows(
                    IllegalStateException::class.java
                ) {
                    client.fetch("US")
                }
                
                assertEquals(
                    "Google Trends RSS request failed: HTTP $statusCode",
                    exception.message
                )

            } finally {
                server.stop(0)
            }
        }
    }
    
    @Test
    fun `Fails when RSS response exceeds request timeout`() {
        val server = HttpServer.create(
            InetSocketAddress("127.0.0.1", 0),
            0
        )
        
        server.createContext("/trending/rss") { exchange ->
            Thread.sleep(1_500)
            exchange.close()
        }
        
        server.start()
        
        try {
            val properties = GoogleTrendsRssProperties(
                baseUrl = "http://127.0.0.1:${server.address.port}/trending/rss",
                connectTimeout = Duration.ofSeconds(1),
                requestTimeout = Duration.ofMillis(500),
            )
            
            val client = GoogleTrendsRssClient(properties)
            
            val exception = assertThrows(
                HttpTimeoutException::class.java
            ) {
                client.fetch("US")
            }

        } finally {
            server.stop(0)
        }
    }
    
    @Test
    fun `Rejects invalid geo before sending HTTP request`() {
        val requestCount = AtomicInteger(0)
        
        val server = HttpServer.create(
            InetSocketAddress("127.0.0.1", 0),
            0
        )
        
        server.createContext("/trending/rss") { exchange ->
            requestCount.incrementAndGet()
            
            val bytes = "<rss/>".toByteArray(StandardCharsets.UTF_8)
            
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
                requestTimeout = Duration.ofMillis(500),
            )
            
            val client = GoogleTrendsRssClient(properties)
            
            val invalidGeos = listOf(
                "kr",
                "USA",
                "1A",
                "",
                " K"
            )
            
            invalidGeos.forEach { geo ->
                assertThrows(
                    IllegalArgumentException::class.java
                ) {
                    client.fetch(geo)
                }
            }
            
            assertEquals(
                0,
                requestCount.get(),
                "Invalid geo should be rejected before HTTP request."
            )
            
        } finally {
            server.stop(0)
        }
    }
}