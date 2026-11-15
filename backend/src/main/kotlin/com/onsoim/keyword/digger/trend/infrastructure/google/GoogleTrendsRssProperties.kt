package com.onsoim.keyword.digger.trend.infrastructure.google

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "keyword-digger.trend.google.rss")
data class GoogleTrendsRssProperties(
    val baseUrl: String,
    val connectTimeout: Duration,
    val requestTimeout: Duration,
)
