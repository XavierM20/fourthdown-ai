export interface StatsTeam {
  id: number;
  name: string;
  abbreviation: string;
  conference: string;
  city: string;
  state: string;
  logoUrl: string;
}

export interface StatsGame {
  id: number;
  gameDate: string;
  status: string;
  venue: string;
}

export interface TeamGameStats {
  id: number;
  game: StatsGame;
  team: StatsTeam;
  points: number;
  totalYards: number;
  passingYards: number;
  rushingYards: number;
  turnovers: number;
  firstDowns: number;
  thirdDownAttempts: number;
  thirdDownConversions: number;
  redZoneAttempts: number;
  redZoneScores: number;
  penalties: number;
  penaltyYards: number;
}

export interface TeamAnalytics {
  teamId: number;
  teamName: string;

  gamesPlayed: number;
  wins: number;
  losses: number;

  averagePoints: number;
  averageTotalYards: number;
  averagePassingYards: number;
  averageRushingYards: number;
  averageTurnovers: number;

  averageScoringMargin: number;
  thirdDownConversionRate: number;
  redZoneScoringRate: number;

  recentWins: number;
  recentLosses: number;
}

export interface TeamComparison {
  teamOne: TeamAnalytics;
  teamTwo: TeamAnalytics;
}


const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export async function getStatsByGame(
  gameId: number
): Promise<TeamGameStats[]> {
  const response = await fetch(`${API_URL}/stats/game/${gameId}`);

  if (!response.ok) {
    throw new Error("Failed to fetch game stats");
  }

  return response.json();
}

export async function getStatsByTeam(
  teamId: number
): Promise<TeamGameStats[]> {
  const response = await fetch(`${API_URL}/stats/team/${teamId}`);

  if (!response.ok) {
    throw new Error("Failed to fetch team stats");
  }

  return response.json();
}

export async function getTeamAnalytics(
  teamId: number
): Promise<TeamAnalytics> {
  const response = await fetch(
    `${API_URL}/analytics/teams/${teamId}`
  );

  if (!response.ok) {
    throw new Error("Failed to fetch team analytics");
  }

  return response.json();
}

export async function compareTeams(
  teamOneId: number,
  teamTwoId: number
): Promise<TeamComparison> {
  const response = await fetch(
    `${API_URL}/analytics/compare?team1=${teamOneId}&team2=${teamTwoId}`
  );

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);

    throw new Error(
      errorData?.message || "Failed to compare teams"
    );
  }

  return response.json();
}