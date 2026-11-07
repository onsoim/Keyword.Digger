import {
  Card,
  CardContent,
  Chip,
  Divider,
  Link,
  Stack,
  Typography,
} from '@mui/material'

import type { TrendingKeyword } from '../types/trend'

interface Props {
  rank: number
  trend: TrendingKeyword
}

export function TrendCard({ rank, trend }: Props) {
  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Stack
            direction="row"
            sx={{
                justifyContent: 'space-between',
                alignItems: 'center',
            }}
          >
            <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
              <Typography variant="h5" color="text.secondary">
                {String(rank).padStart(2, '0')}
              </Typography>

              <Typography variant="h6">
                {trend.keyword}
              </Typography>
            </Stack>

            {trend.approximateTraffic && (
              <Chip
                label={trend.approximateTraffic}
                color="primary"
                variant="outlined"
              />
            )}
          </Stack>

          {trend.startedAt && (
            <Typography variant="body2" color="text.secondary">
               From: {new Date(trend.startedAt).toLocaleString('en-US')} ~
            </Typography>
          )}

          {trend.relatedNews.length > 0 && (
            <>
              <Divider />
              <Typography sx={{ fontWeight: 700 }}>
                Related News
              </Typography>

              {trend.relatedNews.map((news, index) => (
                <Stack key={`${news.url ?? news.title}-${index}`} spacing={0.5}>
                  {news.url ? (
                    <Link
                      href={news.url}
                      target="_blank"
                      rel="noopener noreferrer"
                      underline="hover"
                    >
                      {news.title}
                    </Link>
                  ) : (
                    <Typography>{news.title}</Typography>
                  )}

                  <Typography variant="caption" color="text.secondary">
                    {news.source}
                  </Typography>

                  {news.snippet && (
                    <Typography variant="body2" color="text.secondary">
                      {news.snippet}
                    </Typography>
                  )}
                </Stack>
              ))}
            </>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}