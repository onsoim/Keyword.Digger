import { useQuery } from '@tanstack/react-query'
import { getTrendingKeywords } from '../api/getTrendingKeywords'

export function useTrendingKeywords(geo: string = 'US') {
    return useQuery({
        queryKey: ['trends', geo],
        queryFn: () => getTrendingKeywords(geo),
        staleTime: 60 * 1000,
        retry: 1,
    })
}