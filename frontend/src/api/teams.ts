export interface Team {
  id: number;
  cfbdId: number | null;

  name: string;
  abbreviation: string | null;
  conference: string;

  classification: string | null;

  city: string | null;
  state: string | null;

  logoUrl: string | null;

  mascot: string | null;
  primaryColor: string | null;
  alternateColor: string | null;
}

export interface TeamRecordSegment {
  games: number | null;
  wins: number | null;
  losses: number | null;
  ties: number | null;
}

export interface TeamRecord {
  year: number;
  teamId: number;
  team: string;
  classification: string | null;
  conference: string | null;

  total: TeamRecordSegment | null;
  conferenceGames: TeamRecordSegment | null;
}

const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export async function getTeams(): Promise<Team[]> {
  const response = await fetch(
    `${API_URL}/teams`
  );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch teams"
    );
  }

  return response.json();
}

export async function getTeamById(
  id: number
): Promise<Team> {
  const response = await fetch(
    `${API_URL}/teams/${id}`
  );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch team"
    );
  }

  return response.json();
}

export async function getTeamRecord(
  id: number,
  year: number
): Promise<TeamRecord> {
  const response = await fetch(
    `${API_URL}/teams/${id}/record?year=${year}`
  );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch team record"
    );
  }

  return response.json();
}