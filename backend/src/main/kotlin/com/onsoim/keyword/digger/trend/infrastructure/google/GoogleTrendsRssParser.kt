package com.onsoim.keyword.digger.trend.infrastructure.google

import com.onsoim.keyword.digger.trend.domain.RelatedNews
import com.onsoim.keyword.digger.trend.domain.TrendingKeyword

import org.springframework.stereotype.Component
import java.time.OffsetDateTime


@Component
class GoogleTrendsRssParser {
    
    fun parse(xml: String): List<TrendingKeyword> {
        return listOf(
            TrendingKeyword(
                keyword = "갤럭시",
                trendUrl = "https://trends.google.com/trends/explore?q=갤럭시",
                approximateTraffic = "10,000+",
                startedAt = OffsetDateTime.parse("2026-11-06T09:00:00+09:00"),
                relatedNews = listOf(
                    RelatedNews(
                        title = "삼성전자 갤럭시 신제품 관련 소식",
                        url = "https://example.com/news/1",
                        source = "Example News",
                        snippet = "갤럭시 관련 최신 소식을 소개합니다."
                    )
                )
            ),
            TrendingKeyword(
                keyword = "인공지능",
                trendUrl = "https://trends.google.com/trends/explore?q=인공지능",
                approximateTraffic = "5,000+",
                startedAt = OffsetDateTime.parse("2026-11-06T08:30:00+09:00"),
                relatedNews = listOf(
                    RelatedNews(
                        title = "인공지능 기술 동향",
                        url = "https://example.com/news/2",
                        source = "Example Tech",
                        snippet = "최근 인공지능 기술의 발전과 활용 사례를 다룹니다."
                    )
                )
            )
        )
    }
}