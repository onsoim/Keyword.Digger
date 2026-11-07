import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Container,
  Stack,
  Typography,
} from '@mui/material'

import { useTrendingKeywords } from './features/trends/hooks/useTrendingKeywords'
import { TrendCard } from './features/trends/components/TrendCard'

export default function App() {
  const {
    data,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useTrendingKeywords('US')

  return (
    <Container maxWidth="md">
      <Stack spacing={3} sx={{ py: 5 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700 }}>
            Keyword Digger
          </Typography>

          <Typography color="text.secondary">
            Google Trends · US
          </Typography>
        </Box>

        <Stack direction="row" sx={{ justifyContent: 'space-between' }}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            Hot Topics
          </Typography>

          <Button
            variant="outlined"
            onClick={() => refetch()}
            disabled={isFetching}
          >
            Refresh
          </Button>
        </Stack>

        {isPending && (
          <Box sx={{ textAlign: 'center', py: 5 }}>
            <CircularProgress />
            <Typography sx={{ mt: 2 }}>
              Loading trend data.
            </Typography>
          </Box>
        )}

        {isError && (
          <Alert severity="error">
            Couldn't load data: {error.message}
          </Alert>
        )}

        {data?.length === 0 && (
          <Alert severity="info">
            There are no trends now.
          </Alert>
        )}

        {data?.map((trend, index) => (
          <TrendCard
            key={`${trend.keyword}-${index}`}
            rank={index + 1}
            trend={trend}
          />
        ))}
      </Stack>
    </Container>
  )
}