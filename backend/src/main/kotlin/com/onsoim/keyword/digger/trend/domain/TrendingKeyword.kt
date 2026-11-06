package com.onsoim.keyword.digger.trend.domain

import java.time.OffsetDateTime


data class TrendingKeyword(
    val keyword: String,
    val trendUrl: String?,
    val approximateTraffic: String?,
    val startedAt: OffsetDateTime?,
    val relatedNews: List<RelatedNews>,
)