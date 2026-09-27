export interface PollTeamRanking {
  rank: number;
  teamId: number;
  teamName: string;
  abbreviation: string | null;
  conference: string;
  classification: string;
  logoUrl: string | null;
  wins: number;
  losses: number;
  ties: number;
  pollPoints: number | null;
  firstPlaceVotes: number | null;
}

export interface PollRankingsResponse {
  season: number;
  week: number;
  poll: string;
  classification: string;
  rankings: PollTeamRanking[];
}

export interface RankingHistoryPoint {
  week: number;
  poll: string;
  rank: number | null;
  pollPoints: number | null;
  firstPlaceVotes: number | null;
}

export interface TeamRankingHistoryResponse {
  season: number;
  classification: string;
  teamId: number;
  teamName: string;
  currentRank: number | null;
  previousRank: number | null;
  movement: number | null;
  history: RankingHistoryPoint[];
}

const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export async function getRankings(
  season: number,
  classification: "fbs" | "fcs",
  week?: number
): Promise<PollRankingsResponse> {
  const params =
    new URLSearchParams({
      season:
        season.toString(),
      classification,
    });

  if (week !== undefined) {
    params.set(
      "week",
      week.toString()
    );
  }

  const response =
    await fetch(
      `${API_URL}/rankings?${params.toString()}`
    );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch rankings"
    );
  }

  return response.json();
}

export async function getRankingWeeks(
  season: number,
  classification: "fbs" | "fcs"
): Promise<number[]> {
  const params =
    new URLSearchParams({
      season:
        season.toString(),
      classification,
    });

  const response =
    await fetch(
      `${API_URL}/rankings/weeks?${params.toString()}`
    );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch ranking weeks"
    );
  }

  return response.json();
}

export async function getRankingHistory(
  season: number,
  classification: "fbs" | "fcs",
  teamId: number
): Promise<TeamRankingHistoryResponse> {
  const params =
    new URLSearchParams({
      season:
        season.toString(),
      classification,
      teamId:
        teamId.toString(),
    });

  const response =
    await fetch(
      `${API_URL}/rankings/history?${params.toString()}`
    );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch ranking history"
    );
  }

  return response.json();
}