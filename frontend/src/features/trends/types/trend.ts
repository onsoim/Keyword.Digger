export interface RelatedNews {
    title: string
    url: string | null
    source: string | null
    snippet: string | null
}

export interface TrendingKeyword {
    keyword: string
    trendUrl: string | null
    approximateTraffic: string | null
    startedAt: string | null
    relatedNews: RelatedNews[]
}