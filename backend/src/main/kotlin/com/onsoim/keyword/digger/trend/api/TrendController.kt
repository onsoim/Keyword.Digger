package com.onsoim.keyword.digger.trend.api

import com.onsoim.keyword.digger.trend.application.GetTrendingKeywordsService

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/trends")
class TrendController(
    private val service: GetTrendingKeywordsService,
) {

    @GetMapping
    fun getTrendingKeywords(
        @RequestParam(defaultValue = "US") geo: String,
    ) = service.getTrendingKeywords(geo)
}
