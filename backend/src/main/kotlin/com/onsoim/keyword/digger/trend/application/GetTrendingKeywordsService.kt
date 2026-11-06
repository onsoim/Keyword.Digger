package com.onsoim.keyword.digger.trend.application

import com.onsoim.keyword.digger.trend.domain.TrendingKeyword
import com.onsoim.keyword.digger.trend.infrastructure.google.GoogleTrendsRssClient
import com.onsoim.keyword.digger.trend.infrastructure.google.GoogleTrendsRssParser
import org.springframework.stereotype.Service


@Service
class GetTrendingKeywordsService(
    private val rssClient: GoogleTrendsRssClient,
    private val rssParser: GoogleTrendsRssParser,
) {

    fun getTrendingKeywords(geo: String): List<TrendingKeyword> {
        val xml = rssClient.fetch(geo)

        return rssParser.parse(xml)
    }
}
