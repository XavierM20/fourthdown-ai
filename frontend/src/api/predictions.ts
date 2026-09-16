const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export interface MatchupPrediction {
  homeTeamId: number;
  homeTeamName: string;

  awayTeamId: number;
  awayTeamName: string;

  predictedWinnerId: number;
  predictedWinnerName: string;

  homeWinProbability: number;
  awayWinProbability: number;
  confidence: number;

  projectedHomeScore: number;
  projectedAwayScore: number;

  explanation: string[];
}

/*
 * Manual / hypothetical matchup.
 *
 * Backend uses the current time as the cutoff.
 */
export async function predictMatchup(
  homeTeamId: number,
  awayTeamId: number
): Promise<MatchupPrediction> {

  const response = await fetch(
    `${API_URL}/predictions/matchup` +
      `?homeTeamId=${homeTeamId}` +
      `&awayTeamId=${awayTeamId}`
  );

  if (!response.ok) {
    const message =
      await response.text();

    throw new Error(
      message ||
        "Failed to generate prediction"
    );
  }

  return response.json();
}

/*
 * Real scheduled game.
 *
 * Backend loads the game and uses its
 * scheduled kickoff time as the feature cutoff.
 */
export async function predictGame(
  gameId: number
): Promise<MatchupPrediction> {

  const response = await fetch(
    `${API_URL}/predictions/game/${gameId}`
  );

  if (!response.ok) {
    const message =
      await response.text();

    throw new Error(
      message ||
        "Failed to generate game prediction"
    );
  }

  return response.json();
}