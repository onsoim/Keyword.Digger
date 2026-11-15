package com.onsoim.keyword.digger.trend.infrastructure.google

import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets


@Component
class GoogleTrendsRssClient(
    private val properties: GoogleTrendsRssProperties,
) {

    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(properties.connectTimeout)
        .build()

    fun fetch(geo: String = "US"): String {
        require(geo.matches(Regex("^[A-Z]{2}$"))) {
            "Country code should be capital 2 letters."
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create("${properties.baseUrl}?geo=$geo"))
            .timeout(properties.requestTimeout)
            .header("User-Agent", "Keyword-Digger/0.1")
            .header("Accept", "application/rss+xml, application/xml, text/xml")
            .GET()
            .build()

        val response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        )

        if (response.statusCode() !in 200..299) {
            throw IllegalStateException(
                "Google Trends RSS request failed: HTTP ${response.statusCode()}"
            )
        }

        return response.body()
    }
}
