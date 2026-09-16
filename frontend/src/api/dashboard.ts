export interface DashboardData {
  totalTeams: number;
  totalGames: number;
  upcomingGames: number;
  nextGame: {
    id: number;
    homeTeam: string;
    awayTeam: string;
    gameDate: string;
    venue: string;
  } | null;
}

const API_URL = (
  import.meta.env.VITE_API_URL ??
  "http://localhost:8080/api/v1"
).replace(/\/$/, "");

export async function getDashboardData(): Promise<DashboardData> {
  const response = await fetch(`${API_URL}/dashboard`);

  if (!response.ok) {
    throw new Error("Failed to fetch dashboard data");
  }

  return response.json();
}