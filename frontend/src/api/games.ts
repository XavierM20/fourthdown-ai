export interface GameTeam {
  id: number;
  name: string;
  abbreviation: string;
  conference: string;
  city: string;
  state: string;
  logoUrl: string;
}

export interface Game {
  id: number;

  homeTeam: GameTeam;
  awayTeam: GameTeam;

  gameDate: string;

  season: number | null;
  week: number | null;
  seasonType: string | null;

  gameName: string | null;
  playoffRound: string | null;

  status: string;

  homeScore: number | null;
  awayScore: number | null;

  venue: string | null;
}

const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export async function getGames(): Promise<Game[]> {
  const response =
    await fetch(
      `${API_URL}/games`,
      {
        cache: "no-store",
      }
    );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch games"
    );
  }

  return response.json();
}

export async function getGameById(
  id: number
): Promise<Game> {
  const response =
    await fetch(
      `${API_URL}/games/${id}`,
      {
        cache: "no-store",
      }
    );

  if (!response.ok) {
    throw new Error(
      "Failed to fetch game"
    );
  }

  return response.json();
}