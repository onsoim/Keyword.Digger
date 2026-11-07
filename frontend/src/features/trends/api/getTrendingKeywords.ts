import type { TrendingKeyword } from '../types/trend'

export async function getTrendingKeywords(
    geo: string = 'US',
): Promise<TrendingKeyword[]> {
    const response = await fetch(
        `/api/v1/trends?geo=${encodeURIComponent(geo)}`,
    )

    if (!response.ok) {
        throw new Error(`Trend search failed: HTTP ${response.status}`)
    }

    return response.json() as Promise<TrendingKeyword[]>
}